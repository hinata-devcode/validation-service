package com.venky.validationservice.integration.razorpay;

import org.springframework.stereotype.Component;

import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;
import com.venky.validationservice.reconciliation.ProviderReconciliationHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RazorpayReconciliationHandler implements ProviderReconciliationHandler {

	private final RazorpayClient razorpayClient;
	private final ValidationRequestRepository repository;

	private static final Duration RECONCILIATION_WINDOW = Duration.ofMinutes(30);
	private static final int PAGE_COUNT = 100;
	private static final int MAX_ATTEMPTS=5;

	public RazorpayReconciliationHandler(RazorpayClient razorpayClient, ValidationRequestRepository repository) {
		this.razorpayClient = razorpayClient;
		this.repository = repository;
	}

	@Override
	public String getProvider() {
		return Provider.RAZORPAY.toString();
	}

	@Override
	public void reconcile(List<ValidationRequestEntity> requests) {
		if (requests == null || requests.isEmpty())
			return;

		Instant now = Instant.now();

		// 1. Calculate Global Time Window
		Instant oldestInitiatedAt = requests.get(0).getProviderCallInitiatedAt();
		Instant newestInitiatedAt = requests.get(requests.size() - 1).getProviderCallInitiatedAt();

		Instant globalFrom = oldestInitiatedAt.minusSeconds(60);
		Instant globalTo = newestInitiatedAt.plusSeconds(60);

		// 2. Fetch all Razorpay items in this window (Pagination Loop)
		Map<String, RazorpayValidationItem> razorpayMap = new HashMap<>();
		int skip = 0;

		while (true) {
			// Convert Instants to Unix timestamps (seconds) as required by Razorpay
			long fromUnix = globalFrom.getEpochSecond();
			long toUnix = globalTo.getEpochSecond();

			RazorpayValidationCollectionResponse response = razorpayClient.fetchValidations(fromUnix, toUnix,
					PAGE_COUNT, skip);

			if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
				break; // No more records
			}

			// Build the O(M) lookup map
			for (RazorpayValidationItem item : response.getItems()) {
				if (item.getReferenceId() != null) {
					razorpayMap.put(item.getReferenceId(), item);
				}
			}

			if (response.getItems().size() < PAGE_COUNT) {
				break; // Last page reached
			}
			skip += PAGE_COUNT;
		}

		// 3. Match and Update Database Records O(N)
		for (ValidationRequestEntity request : requests) {
			try {
				String localReferenceId = request.getValidationRequestId().toString();
				RazorpayValidationItem matchedItem = razorpayMap.get(localReferenceId);

				if (matchedItem != null) {
					// Match Found! Link the ID and set to PROCESSING
					request.setProviderReferenceId(matchedItem.getId());
					request.setExecutionStatus(ExecutionStatus.PROCESSING);
					request.setLastStatusCheckAt(now);
				} else {
					// No Match Found. Increment attempts and check expiry
					request.incrementPollAttempts();
					request.setLastStatusCheckAt(now);

					boolean isExpired = now.isAfter(request.getProviderCallInitiatedAt().plus(RECONCILIATION_WINDOW));
					if (isExpired || request.getPollAttempts() >= MAX_ATTEMPTS) {
						request.setExecutionStatus(ExecutionStatus.FAILED);
						request.setFailureOrigin(FailureOrigin.SYSTEM_TIMEOUT);
					}
				}
				repository.save(request);
			} catch (Exception e) {
				System.err.println("Failed to process/save reconcillation " + request.getValidationRequestId() + " - "
						+ e.getMessage());
			}
		}

	}
}
