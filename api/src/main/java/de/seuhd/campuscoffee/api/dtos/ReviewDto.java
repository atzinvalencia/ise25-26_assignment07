package de.seuhd.campuscoffee.api.dtos;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Builder(toBuilder = true)
public record ReviewDto(
        @Nullable Long id,           // null when creating
        @NonNull Long posId,         // ID of POS
        @NonNull Long authorId,      // ID of user
        @NonNull String review,      // review text
        @Nullable Boolean approved   // null on creation (set by backend)
) implements Dto<Long> {

    @Override
    public @Nullable Long getId() {
        return id;
    }
}