package de.seuhd.campuscoffee.api.mapper;

import de.seuhd.campuscoffee.api.dtos.ReviewDto;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * MapStruct mapper for converting between Review domain objects and ReviewDto.
 */
@Mapper(componentModel = "spring")
@ConditionalOnMissingBean
public abstract class ReviewDtoMapper implements DtoMapper<Review, ReviewDto> {

    // Spring injects these automatically (no @Autowired needed)
    protected PosDataService posDataService;
    protected UserDataService userDataService;

    // ---------- DOMAIN → DTO ----------
    @Mapping(target = "posId", expression = "java(source.pos().id())")
    @Mapping(target = "authorId", expression = "java(source.author().id())")
    public abstract ReviewDto fromDomain(Review source);

    // ---------- DTO → DOMAIN ----------
    @Mapping(target = "pos", expression = "java(posDataService.getById(source.posId()))")
    @Mapping(target = "author", expression = "java(userDataService.getById(source.authorId()))")
    @Mapping(target = "approvalCount", expression = "java(0)")
    @Mapping(target = "approved", expression = "java(false)")
    public abstract Review toDomain(ReviewDto source);
}