package dev.linkops.server.dto;

import java.util.List;

/** 导入结果:解析条数、新入库、列表内重复、库内已存在、无效 URL。 */
public record ImportResult(int parsed, int inserted, int duplicateInList, int alreadyInDb, List<String> invalid) {}
