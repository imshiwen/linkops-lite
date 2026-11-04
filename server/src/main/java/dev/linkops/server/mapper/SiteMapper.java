package dev.linkops.server.mapper;

import dev.linkops.server.entity.Site;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SiteMapper {

    List<Site> list();

    Site findById(@Param("id") long id);

    int insert(Site site);

    int update(Site site);
}
