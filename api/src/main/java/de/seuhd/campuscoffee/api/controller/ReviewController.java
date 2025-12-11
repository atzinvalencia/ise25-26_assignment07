package de.seuhd.campuscoffee.api.controller;

import de.seuhd.campuscoffee.api.dtos.ReviewDto;
import de.seuhd.campuscoffee.api.mapper.ReviewDtoMapper;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewDtoMapper mapper;

    // ----------------------------------------------------------
    // GET /api/reviews
    // ----------------------------------------------------------
    @GetMapping
    public @NonNull ResponseEntity<List<ReviewDto>> getAll() {
        return ResponseEntity.ok(
                reviewService.getAll().stream()
                        .map(mapper::fromDomain)
                        .toList()
        );
    }

    // ----------------------------------------------------------
    // GET /api/reviews/{id}
    // ----------------------------------------------------------
    @GetMapping("/{id}")
    public @NonNull ResponseEntity<ReviewDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.fromDomain(reviewService.getById(id))
        );
    }

    // ----------------------------------------------------------
    // POST /api/reviews
    // ----------------------------------------------------------
    @PostMapping
    public @NonNull ResponseEntity<ReviewDto> create(@RequestBody ReviewDto dto) {
        var created = reviewService.upsert(mapper.toDomain(dto));
        return ResponseEntity.ok(mapper.fromDomain(created));
    }

    // ----------------------------------------------------------
    // PUT /api/reviews/{id}
    // ----------------------------------------------------------
    @PutMapping("/{id}")
    public @NonNull ResponseEntity<ReviewDto> update(
            @PathVariable Long id,
            @RequestBody ReviewDto dto
    ) {
        // Domain upsert works for update as well
        var updated = reviewService.upsert(
                mapper.toDomain(dto.toBuilder().id(id).build())
        );
        return ResponseEntity.ok(mapper.fromDomain(updated));
    }

    // ----------------------------------------------------------
    // DELETE /api/reviews/{id}
    // ----------------------------------------------------------
    @DeleteMapping("/{id}")
    public @NonNull ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }



    // ----------------------------------------------------------
    // GET /api/reviews/filter?pos_id=XXX&approved=true/false
    // ----------------------------------------------------------
    @GetMapping("/filter")
    public @NonNull ResponseEntity<List<ReviewDto>> filter(
            @RequestParam("pos_id") Long posId,
            @RequestParam("approved") Boolean approved
    ) {
        return ResponseEntity.ok(
                reviewService.filter(posId, approved).stream()
                        .map(mapper::fromDomain)
                        .toList()
        );
    }

    // ----------------------------------------------------------
    // POST /api/reviews/{id}/approve?user_id=XXX
    // ----------------------------------------------------------
    @PostMapping("/{id}/approve")
    public @NonNull ResponseEntity<ReviewDto> approve(
            @PathVariable Long id,
            @RequestParam("user_id") Long userId
    ) {
        var review = reviewService.getById(id);
        var updated = reviewService.approve(review, userId);
        return ResponseEntity.ok(mapper.fromDomain(updated));
    }
}