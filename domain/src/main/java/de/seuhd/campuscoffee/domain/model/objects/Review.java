package de.seuhd.campuscoffee.domain.model.objects;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import de.seuhd.campuscoffee.domain.model.objects.Pos;
import de.seuhd.campuscoffee.domain.model.objects.User;

/**
 * Domain record representing a review for a POS.
 */
@Builder(toBuilder = true)
public record Review(
        @Nullable Long id,                   // null when newly created
        @NonNull Pos pos,                    // POS being reviewed
        @NonNull User author,                // User who wrote the review
        @NonNull String review,              // Review text
        @NonNull Integer approvalCount,      // increases when approved
        @NonNull Boolean approved            // set by domain logic
) implements DomainModel<Long> {

    @Override
    public Long getId() {
        return id;
    }
}