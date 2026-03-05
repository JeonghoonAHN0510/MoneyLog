package com.moneylog_backend.moneylog.transaction.importer;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HeaderProfileResolver {
    private static final String HEADER_PROFILE_RESOURCE = "/transaction-import-header-profile.json";
    private static final HeaderImportProfile HEADER_PROFILE = loadHeaderProfile();

    public HeaderResolution resolve (List<List<String>> rawRows) {
        int headerRowIndex = resolveHeaderRowIndex(rawRows);
        Map<String, Integer> headerIndex = headerRowIndex >= 0 ? resolveHeaderIndex(rawRows.get(headerRowIndex)) : Map.of();
        Map<String, String> headerLabelByField = headerRowIndex >= 0
            ? resolveHeaderLabelByField(rawRows.get(headerRowIndex), headerIndex)
            : Map.of();
        int startRow = headerRowIndex >= 0 ? headerRowIndex + 1 : 0;
        return new HeaderResolution(headerIndex, headerLabelByField, startRow);
    }

    public Map<String, Integer> resolveHeaderIndex (List<String> headerRow) {
        Map<String, Integer> found = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String token = normalize(headerRow.get(i));
            if (token.isBlank()) {
                continue;
            }
            String field = detectHeaderField(token);
            if (field != null) {
                found.put(field, i);
            }
        }
        return found;
    }

    public String detectHeaderField (String normalizedToken) {
        String bestField = null;
        int bestMatchScore = 0;
        int bestPriority = Integer.MIN_VALUE;
        int bestOrderIndex = Integer.MAX_VALUE;

        List<String> fieldMatchOrder = HEADER_PROFILE.fieldMatchOrder();
        for (int i = 0; i < fieldMatchOrder.size(); i++) {
            String field = fieldMatchOrder.get(i);
            Set<String> aliases = HEADER_PROFILE.aliasesFor(field);
            int matchScore = scoreAliasMatch(normalizedToken, aliases);
            if (matchScore == 0) {
                continue;
            }

            int priority = headerFieldPriority(field);
            boolean betterCandidate = matchScore > bestMatchScore
                || (matchScore == bestMatchScore && priority > bestPriority)
                || (matchScore == bestMatchScore && priority == bestPriority && i < bestOrderIndex);

            if (betterCandidate) {
                bestField = field;
                bestMatchScore = matchScore;
                bestPriority = priority;
                bestOrderIndex = i;
            }
        }
        return bestField;
    }

    private int resolveHeaderRowIndex (List<List<String>> rawRows) {
        int bestRow = -1;
        int bestScore = 0;

        int maxScan = Math.min(rawRows.size(), HEADER_PROFILE.headerScanLimit());
        for (int i = 0; i < maxScan; i++) {
            List<String> row = rawRows.get(i);
            int score = headerRowScore(row);
            if (score >= HEADER_PROFILE.headerMatchThreshold() && score > bestScore) {
                bestScore = score;
                bestRow = i;
            }
        }
        return bestRow;
    }

    private int headerRowScore (List<String> row) {
        Set<String> matchedFields = new HashSet<>();
        for (String rawCell : row) {
            String token = normalize(rawCell);
            if (token.isBlank()) {
                continue;
            }
            String field = detectHeaderField(token);
            if (field != null) {
                matchedFields.add(field);
            }
        }
        return matchedFields.size();
    }

    private Map<String, String> resolveHeaderLabelByField (List<String> headerRow, Map<String, Integer> headerIndex) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            int columnIndex = entry.getValue();
            if (columnIndex >= 0 && columnIndex < headerRow.size()) {
                headers.put(entry.getKey(), headerRow.get(columnIndex).trim());
            }
        }
        return headers;
    }

    private int scoreAliasMatch (String normalizedToken, Set<String> candidateTokens) {
        int best = 0;
        for (String token : candidateTokens) {
            String normalizedAlias = normalize(token);
            if (normalizedAlias.isBlank()) {
                continue;
            }
            if (normalizedToken.equals(normalizedAlias)) {
                return 2;
            }
            if (normalizedToken.contains(normalizedAlias)) {
                best = Math.max(best, 1);
            }
        }
        return best;
    }

    private int headerFieldPriority (String field) {
        if ("debitAmount".equals(field) || "creditAmount".equals(field)) {
            return 2;
        }
        if ("amount".equals(field)) {
            return 1;
        }
        return 0;
    }

    private String normalize (String raw) {
        if (raw == null) {
            return "";
        }
        String value = Normalizer.normalize(raw.trim().toLowerCase(), Normalizer.Form.NFKC);
        return value.replaceAll("\\s+", "");
    }

    private static HeaderImportProfile loadHeaderProfile () {
        try (InputStream inputStream = HeaderProfileResolver.class.getResourceAsStream(HEADER_PROFILE_RESOURCE)) {
            if (inputStream == null) {
                return HeaderImportProfile.createDefault();
            }

            JsonNode root = new ObjectMapper().readTree(inputStream);
            int headerScanLimit = root.path("headerScanLimit").asInt(40);
            int headerMatchThreshold = root.path("headerMatchThreshold").asInt(2);

            Map<String, Set<String>> aliases = new HashMap<>();
            JsonNode aliasesNode = root.path("aliases");
            if (aliasesNode.isObject()) {
                aliasesNode.fieldNames()
                           .forEachRemaining(field -> aliases.put(field, parseAliasSet(aliasesNode.path(field))));
            }

            List<String> fieldMatchOrder = new ArrayList<>();
            JsonNode orderNode = root.path("fieldMatchOrder");
            if (orderNode.isArray()) {
                for (JsonNode token : orderNode) {
                    if (token.isTextual()) {
                        fieldMatchOrder.add(token.asText());
                    }
                }
            }
            if (fieldMatchOrder.isEmpty()) {
                fieldMatchOrder = List.of(
                    "tradingAt",
                    "tradingTime",
                    "title",
                    "debitAmount",
                    "creditAmount",
                    "amount",
                    "accountName",
                    "categoryName",
                    "paymentName",
                    "memo"
                );
            }

            return new HeaderImportProfile(
                headerScanLimit,
                headerMatchThreshold,
                aliases,
                fieldMatchOrder
            );
        } catch (Exception e) {
            return HeaderImportProfile.createDefault();
        }
    }

    private static Set<String> parseAliasSet (JsonNode node) {
        Set<String> aliasSet = new HashSet<>();
        if (!node.isArray()) {
            return Set.of();
        }
        for (JsonNode element : node) {
            if (element.isTextual()) {
                String value = element.asText();
                if (!value.isBlank()) {
                    aliasSet.add(value);
                }
            }
        }
        return aliasSet;
    }

    public record HeaderResolution (
        Map<String, Integer> headerIndex,
        Map<String, String> headerLabelByField,
        int startRow
    ) {}

    private static class HeaderImportProfile {
        private final int headerScanLimit;
        private final int headerMatchThreshold;
        private final Map<String, Set<String>> aliases;
        private final List<String> fieldMatchOrder;

        private HeaderImportProfile (int headerScanLimit,
                                     int headerMatchThreshold,
                                     Map<String, Set<String>> aliases,
                                     List<String> fieldMatchOrder) {
            this.headerScanLimit = headerScanLimit;
            this.headerMatchThreshold = headerMatchThreshold;
            this.aliases = aliases;
            this.fieldMatchOrder = fieldMatchOrder;
        }

        private static HeaderImportProfile createDefault () {
            Map<String, Set<String>> aliases = new HashMap<>();
            aliases.put("tradingAt", Set.of("거래일", "거래일자", "일자", "날짜", "date"));
            aliases.put("tradingTime", Set.of("거래시간", "시간"));
            aliases.put("title", Set.of("title", "제목", "내용", "적요"));
            aliases.put("amount", Set.of("amount", "금액"));
            aliases.put("debitAmount", Set.of("출금", "출금(원)", "출금금액", "출금금액(원)"));
            aliases.put("creditAmount", Set.of("입금", "입금(원)", "입금금액", "입금금액(원)"));
            aliases.put("accountName", Set.of("account", "계좌", "계좌명"));
            aliases.put("categoryName", Set.of("category", "카테고리"));
            aliases.put("paymentName", Set.of("payment", "결제수단"));
            aliases.put("memo", Set.of("memo", "메모", "거래점"));

            return new HeaderImportProfile(
                40,
                2,
                aliases,
                List.of(
                    "tradingAt",
                    "tradingTime",
                    "title",
                    "debitAmount",
                    "creditAmount",
                    "amount",
                    "accountName",
                    "categoryName",
                    "paymentName",
                    "memo"
                )
            );
        }

        private int headerScanLimit () {
            return headerScanLimit;
        }

        private int headerMatchThreshold () {
            return headerMatchThreshold;
        }

        private Set<String> aliasesFor (String field) {
            return Optional.ofNullable(aliases.get(field)).orElse(Set.of());
        }

        private List<String> fieldMatchOrder () {
            return fieldMatchOrder;
        }
    }
}
