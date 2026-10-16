package dev.linkops.server.mapper;

import dev.linkops.server.dto.SourceStats;
import dev.linkops.server.entity.BacklinkSource;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BacklinkSourceMapper {

    /** 多值批量插入,normalized_url 冲突时跳过;返回实际插入行数。 */
    int insertIgnoreBatch(@Param("list") List<BacklinkSource> list);

    List<BacklinkSource> list(@Param("limit") int limit);

    SourceStats stats();
}
