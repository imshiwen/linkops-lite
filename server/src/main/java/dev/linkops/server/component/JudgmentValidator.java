package dev.linkops.server.component;

import dev.linkops.server.dto.JudgmentResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 业务规则校验。Spring AI 的 validateSchema() 只保证 JSON 结构对;
 * 字段值是否合理(分数范围、理由是否为空、结论和分数是否矛盾)需要我们自己校验。
 */
@Component
public class JudgmentValidator {

    /**
     * 返回所有违反的规则,空列表表示通过。
     * TODO(你来写):按你设计的字段写规则,例如分数必须在 0-100、理由不能为空、
     * suitable=true 时分数不应低于某个阈值。
     */
    public List<String> validate(JudgmentResult r) {
        List<String> errors = new ArrayList<>();
        if (r == null) {
            errors.add("结果为空");
            return errors;
        }
        if (r.reasons() == null || r.reasons().isEmpty()) errors.add("reasons 不能为空");
        return errors;
    }
}
