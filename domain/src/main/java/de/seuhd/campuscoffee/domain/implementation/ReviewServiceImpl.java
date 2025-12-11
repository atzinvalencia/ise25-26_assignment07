package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import de.seuhd.campuscoffee.domain.ports.data.ReviewDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.seuhd.campuscoffee.domain.exceptions.ValidationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;

import java.util.List;

/**
 * Implementation of the Review service that handles business logic related to review entities.
 */
@Slf4j
@Service
public class ReviewServiceImpl extends CrudServiceImpl<Review, Long> implements ReviewService {
    private final ReviewDataService reviewDataService;
    private final UserDataService userDataService;
    private final PosDataService posDataService;
    // TODO: Try to find out the purpose of this class and how it is connected to the application.yaml configuration file.
    private final ApprovalConfiguration approvalConfiguration;

    public ReviewServiceImpl(@NonNull ReviewDataService reviewDataService,
                             @NonNull UserDataService userDataService,
                             @NonNull PosDataService posDataService,
                             @NonNull ApprovalConfiguration approvalConfiguration) {
        super(Review.class);
        this.reviewDataService = reviewDataService;
        this.userDataService = userDataService;
        this.posDataService = posDataService;
        this.approvalConfiguration = approvalConfiguration;
    }

    @Override
    protected CrudDataService<Review, Long> dataService() {
        return reviewDataService;
    }

    @Override
    @Transactional
    public @NonNull Review upsert(@NonNull Review review) {
        // 1) Validate that the POS exists (throws NotFoundException if not)
        var pos = posDataService.getById(review.pos().getId());

        // 2) Ensure the user has at most one review per POS
        //    Allow update of the same review (same id), but not a second one.
        var existingReviews = reviewDataService.filter(pos, review.author());

        boolean anotherReviewExists = existingReviews.stream()
                .anyMatch(existing ->
                        existing.id() != null
                                && (review.id() == null || !existing.id().equals(review.id()))
                );

        if (anotherReviewExists) {
            throw new ValidationException("User already created a review for this POS");
        }

        // 3) Use the persisted POS instance in the review and delegate to base upsert
        var reviewToSave = review.toBuilder()
                .pos(pos)
                .build();

        return super.upsert(reviewToSave);
    }


    @Override
    @Transactional(readOnly = true)
    public @NonNull List<Review> filter(@NonNull Long posId, @NonNull Boolean approved) {
        return reviewDataService.filter(posDataService.getById(posId), approved);
    }

    @Override
    @Transactional
    public @NonNull Review approve(@NonNull Review review, @NonNull Long userId) {
        log.info("Processing approval request for review with ID '{}' by user with ID '{}'...",
                review.getId(), userId);

        // 1) Validate that the user exists
        var user = userDataService.getById(userId);

        // 2) Validate that the review exists (load the persisted review)
        var existingReview = reviewDataService.getById(review.getId());

        // 3) A user cannot approve their own review
        if (existingReview.author() != null
                && existingReview.author().id() != null
                && existingReview.author().id().equals(user.id())) {
            throw new ValidationException("User cannot approve their own review");
        }

        // 4) Increment approval count
        var updatedReview = existingReview.toBuilder()
                .approvalCount(existingReview.approvalCount() + 1)
                .build();

        // 5) Update approval status according to the configured threshold
        updatedReview = updateApprovalStatus(updatedReview);

        // 6) Persist and return
        return reviewDataService.upsert(updatedReview);
    }

    /**
     * Calculates and updates the approval status of a review based on the approval count.
     * Business rule: A review is approved when it reaches the configured minimum approval count threshold.
     *
     * @param review The review to calculate approval status for
     * @return The review with updated approval status
     */
    Review updateApprovalStatus(Review review) {
        log.debug("Updating approval status of review with ID '{}'...", review.getId());
        return review.toBuilder()
                .approved(isApproved(review))
                .build();
    }

    /**
     * Determines if a review meets the minimum approval threshold.
     *
     * @param review The review to check
     * @return true if the review meets or exceeds the minimum approval count, false otherwise
     */
    private boolean isApproved(Review review) {
        return review.approvalCount() >= approvalConfiguration.minCount();
    }
}