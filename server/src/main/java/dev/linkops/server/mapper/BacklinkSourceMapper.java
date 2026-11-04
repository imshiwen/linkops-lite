package dev.linkops.server.mapper;

import dev.linkops.server.dto.LinkCheckResult;
import dev.linkops.server.dto.SourceStats;
import dev.linkops.server.entity.BacklinkSource;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BacklinkSourceMapper {

    /** 多值批量插入,normalized_url 冲突时跳过;返回实际插入行数。 */
    int insertIgnoreBatch(@Param("list") List<BacklinkSource> list);

    /** 列表筛选:status、channel、keyword 均可为空。 */
    List<BacklinkSource> list(@Param("status") String status, @Param("channel") String channel,
                              @Param("keyword") String keyword, @Param("limit") int limit);

    int updateChannel(@Param("id") long id, @Param("channel") String channel);

    List<java.util.Map<String, Object>> countByChannel();

    BacklinkSource findById(@Param("id") long id);

    SourceStats stats();

    /** 按初筛状态取待检查的入口;status 为空时取全部。 */
    List<BacklinkSource> listByScreenStatus(@Param("status") String status, @Param("limit") int limit);

    /** 批量回写检查结果。 */
    int updateCheckResults(@Param("list") List<LinkCheckResult> list);

    List<java.util.Map<String, Object>> countByScreenStatus();
}
