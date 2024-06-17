package com.longfor.plm.project.enums;

import com.google.common.collect.Lists;
import com.longfor.gaia.gfs.core.exception.LFBizException;
import com.longfor.plm.project.constant.CommonConstant;
import com.longfor.plm.project.dto.board.response.C1DataDashboardResponse;
import com.longfor.plm.project.dto.board.response.C1IndexDashboardResponse;
import com.longfor.plm.project.service.board.BaseFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.longfor.plm.project.dto.board.response.C1DataDashboardResponse.Level;

/**
 * @author : lilei
 * @description: 数据看板分组枚举
 * @date : 2022/7/4 14:21
 */
public enum C1IndexGroupEnum implements BaseFunction<C1IndexDashboardResponse, C1IndexDashboardResponse, C1DataDashboardResponse> {

    LEVEL {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> StringUtils.isNotBlank(s.getLevel());
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return C1IndexDashboardResponse::getLevel;
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (Map<String, List<C1IndexDashboardResponse>> map, C1DataDashboardResponse c) -> {
                c.setTotal(map.size());
                c.setItems(Lists.newArrayList());

                long total = map.values().stream().mapToLong(Collection::size).sum();
                map.forEach((s, c1) -> {
                    double houseAreaTotal = c1.stream().mapToDouble(value -> value.obtainLatestHouseArea().doubleValue()).sum();
                    double houseNumTotal = c1.stream().mapToDouble(value -> value.obtainLatestHouseNum().doubleValue()).sum();

                    double slotRate = c1.stream()
                            .mapToDouble(value ->
                                    CommonConstant.SLASH_SPLITTER.splitToList(StringUtils.defaultIfBlank(value.getScPlotR(), StringUtils.EMPTY)).stream()
                                            .mapToDouble(Double::valueOf)
                                            .average()
                                            .orElse(0))
                            .average()
                            .orElse(0);
                    double buildingArea = c1.stream().mapToDouble(value -> value.obtainLatestBuildingArea().doubleValue()).average().orElse(0);
                    Level l = Level.builder()
                            .code(s)
                            .total(c1.size())
                            .percent(BigDecimal.valueOf(c1.size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                            .houseAreaAvg(houseNumTotal == 0 ? BigDecimal.ZERO : new BigDecimal(houseAreaTotal / houseNumTotal).setScale(2, RoundingMode.HALF_UP))
                            .buildingAreaAvg(new BigDecimal(buildingArea).setScale(2, RoundingMode.HALF_UP))
                            .slotRateAvg(new BigDecimal(slotRate).setScale(2, RoundingMode.HALF_UP))
                            .phaseInfoList(c1.stream()
                                    .map(r -> C1DataDashboardResponse.PhaseInfo.builder()
                                            .company(r.getCompany())
                                            .city(r.getCity())
                                            .phaseName(r.getPhaseName())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                    c.getItems().add(l);
                });

                // 按等级排序, 如果增加等级前缀，则在CommonConstant.LEVEL_SORT_MAP扩展
                List<Level> levels = c.getItems().stream()
                        .sorted(Comparator.comparingInt((ToIntFunction<Level>) v ->
                                        CommonConstant.LEVEL_SORT_MAP.getOrDefault(v.getCode().substring(0, 1), CommonConstant.NINETY_NINE))
                                .thenComparing(Level::getCode))
                        .collect(Collectors.toList());
                c.setItems(levels);
            };
        }
    },

    GROUP6_6 {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> StringUtils.isNotBlank(s.getTag6_6()) && Label_6_6Enum.bestOf(s.getTag6_6()) != null;
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return s -> Label_6_6Enum.bestOf(s.getTag6_6()).getDesc();
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (map, resp) -> {
                long total = map.values().stream().mapToLong(Collection::size).sum();
                Level l = resp.getItems().stream().filter(level -> level.getCode().equals(resp.getLevelTmp())).findFirst().orElse(new Level());
                l.setGroup6_6List(Lists.newArrayList());
                map.entrySet().stream()
                        .map(s -> C1DataDashboardResponse.Label6_6.builder()
                                .code(s.getKey())
                                .total(s.getValue().size())
                                .rgba(s.getValue().stream().map(C1IndexDashboardResponse::getTagRgba).distinct().filter(Objects::nonNull).findFirst().orElse(Label_6_6Enum.ofDesc(s.getKey()).getColor()))
                                .percent(BigDecimal.valueOf(s.getValue().size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                                .build())
                        .sorted(Comparator.comparingInt(s -> Label_6_6Enum.ofDesc(s.getCode()).getOrder()))
                        .forEach(l1 -> l.getGroup6_6List().add(l1));
            };
        }
    },

    LABEL6_6 {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> StringUtils.isNotBlank(s.getTag6_6()) && Label_6_6Enum.firstCode(s.getTag6_6()) != null;
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return s -> Label_6_6Enum.firstCode(s.getTag6_6());
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (map, resp) -> {
                Level l = resp.getItems().stream().filter(level -> level.getCode().equals(resp.getLevelTmp())).findFirst().orElse(new Level());
                l.setLabel6_6List(Lists.newArrayList());
                long total = map.values().stream().mapToLong(Collection::size).sum();
                map.entrySet().stream()
                        .map(s -> C1DataDashboardResponse.Label6_6.builder()
                                .code(s.getKey())
                                .total(s.getValue().size())
                                .group(Label_6_6Enum.of(s.getKey()).getDesc())
                                .rgba(s.getValue().stream().map(C1IndexDashboardResponse::getTagRgba).distinct().filter(Objects::nonNull).findFirst().orElse(Label_6_6Enum.bestOf(s.getKey()).getColor()))
                                .percent(BigDecimal.valueOf(s.getValue().size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                                .build())
                        .sorted(Comparator.comparingInt(s -> Label_6_6Enum.of(s.getCode()).getOrder()))
                        .forEach(l1 -> l.getLabel6_6List().add(l1));
            };
        }
    },

    FORMATS {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> {
                if (CollectionUtils.isEmpty(s.obtainLatestFormats())) {
                    return false;
                }
                // 移除纯非住宅的分期
                List<String> list = s.obtainLatestFormats();
                list.removeIf(f -> !f.startsWith("住宅-"));
                return CollectionUtils.isNotEmpty(list);
            };
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return s -> CommonConstant.COMMA_JOINER.join(s.obtainLatestFormats().stream()
                    .filter(s1 -> s1.startsWith("住宅-"))
                    .map(s1 -> StringUtils.substringBeforeLast(s1, ".").replace("住宅-", StringUtils.EMPTY))
                    .distinct()
                    .sorted(Comparator.comparing(s1 -> HouseFormatEnum.of(s1).getOrder()))
                    .collect(Collectors.toList()));
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (map, resp) -> {
                long total = map.values().stream().mapToLong(Collection::size).sum();
                Level l = resp.getItems().stream().filter(level -> level.getCode().equals(resp.getLevelTmp())).findFirst().orElse(new Level());
                l.setFormatsList(Lists.newArrayList());

                map.entrySet().stream()
                        .map(s -> C1DataDashboardResponse.Formats.builder()
                                .code(s.getKey())
                                .total(s.getValue().size())
                                .percent(BigDecimal.valueOf(s.getValue().size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                                .build())
                        .sorted(Comparator.comparingInt(C1DataDashboardResponse.Formats::orderByPrefix).thenComparingInt(C1DataDashboardResponse.Formats::orderByLength))
                        .forEach(f -> l.getFormatsList().add(f));
            };
        }
    },

    BUILDING_AREA {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> Objects.nonNull(s.getStageArea());
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return s -> Arrays.stream(BuildingAreaEnum.values())
                    .filter(b -> b.getF().test(s.getStageArea()))
                    .findFirst()
                    .map(BuildingAreaEnum::getCode)
                    .orElse(BuildingAreaEnum.T1.getCode());
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (map, resp) -> {
                long total = map.values().stream().mapToLong(Collection::size).sum();
                Level l = resp.getItems().stream().filter(level -> level.getCode().equals(resp.getLevelTmp())).findFirst().orElse(new Level());
                l.setBuildingAreaList(Lists.newArrayList());
                map.forEach((s, c1) -> l.getBuildingAreaList().add(C1DataDashboardResponse.BuildingArea.builder()
                        .code(s)
                        .total(c1.size())
                        .percent(BigDecimal.valueOf(c1.size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                        .build()));
            };
        }
    },

    SLOT_RATE {
        @Override
        public Predicate<C1IndexDashboardResponse> filter() {
            return s -> StringUtils.isNotBlank(s.getScPlotR());
        }

        @Override
        public Function<C1IndexDashboardResponse, String> groupKey() {
            return s -> Arrays.stream(SlotRateEnum.values())
                    .filter(b -> b.getF().test(BigDecimal.valueOf(CommonConstant.SLASH_SPLITTER.splitToList(s.getScPlotR()).stream()
                            .mapToDouble(Double::valueOf).average().orElse(0)).setScale(2, RoundingMode.HALF_UP)))
                    .findFirst()
                    .map(SlotRateEnum::getCode)
                    .orElse(SlotRateEnum.T1.getCode());
        }

        @Override
        public Function<C1IndexDashboardResponse, C1IndexDashboardResponse> mapping() {
            return Function.identity();
        }

        @Override
        public BiConsumer<Map<String, List<C1IndexDashboardResponse>>, C1DataDashboardResponse> biConsumer() {
            return (map, resp) -> {
                long total = map.values().stream().mapToLong(Collection::size).sum();
                Level l = resp.getItems().stream().filter(level -> level.getCode().equals(resp.getLevelTmp())).findFirst().orElse(new Level());
                l.setSlotRateList(Lists.newArrayList());
                map.forEach((s, c1) -> l.getSlotRateList().add(C1DataDashboardResponse.SlotRate.builder()
                        .code(s)
                        .total(c1.size())
                        .percent(BigDecimal.valueOf(c1.size()).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                        .build()));
            };
        }
    },
    ;

    @Getter
    @AllArgsConstructor
    public enum BuildingAreaEnum {
        T1("<2万m²", b -> b == null || b.compareTo(new BigDecimal(20000)) < 0),
        T2("2~5万m²", b -> b.compareTo(new BigDecimal(20000)) >= 0 && b.compareTo(new BigDecimal(50000)) <= 0),
        T3(">5万m²", b -> b.compareTo(new BigDecimal(50000)) > 0),
        ;
        private String code;

        private Predicate<BigDecimal> f;

    }

    @Getter
    @AllArgsConstructor
    public enum SlotRateEnum {
        T1("1.0~1.5", b -> b == null || b.compareTo(new BigDecimal(1)) < 0 && b.compareTo(new BigDecimal("1.5")) < 0),
        T2("1.5~2", b -> b.compareTo(new BigDecimal("1.5")) >= 0 && b.compareTo(new BigDecimal(2)) <= 0),
        T3("2~2.5", b -> b.compareTo(new BigDecimal(2)) >= 0 && b.compareTo(new BigDecimal("2.5")) <= 0),
        T4("2.5~3", b -> b.compareTo(new BigDecimal("2.5")) >= 0 && b.compareTo(new BigDecimal(3)) <= 0),
        T5(">3", b -> b.compareTo(new BigDecimal(3)) > 0),
        ;
        private String code;

        private Predicate<BigDecimal> f;

    }

    @Getter
    @AllArgsConstructor
    public enum HouseFormatEnum {
        SINGLE_VILLA("C1-A-01", "独栋别墅", 1),
        ROW_VILLA("C1-A-02", "联排别墅", 2),
        FLEX_VILLA("C1-A-03", "叠拼别墅", 3),
        GARDEN_HOUSE("C1-A-04", "洋房", 4),
        MID_HIGH_HOUSE("C1-A-05", "小高层", 5),
        HIGH_HOUSE("C1-A-08", "高层", 6),
        SUPER_HIGH_HOUSE("C1-A-09", "超高层", 7),
        ;

        private String code;
        private String desc;
        private Integer order;

        public static HouseFormatEnum of(String desc) {
            return Arrays.stream(HouseFormatEnum.values()).filter(s -> StringUtils.equals(desc, s.desc)).findFirst().orElse(SUPER_HIGH_HOUSE);
        }

        public static HouseFormatEnum ofPrefix(String descGroup) {
            return Arrays.stream(HouseFormatEnum.values()).filter(s -> descGroup.startsWith(s.getDesc())).findFirst().orElse(SUPER_HIGH_HOUSE);
        }

    }

    @Getter
    @AllArgsConstructor
    public enum Label_6_6Enum {
        T1("核心区域", Lists.newArrayList("1T", "1U2", "1U1", "2T", "2U2", "3T"), "#FF7D00",1),
        T2("高能级区域", Lists.newArrayList("1C2", "1C1", "2U1", "2C2", "3U2", "3U1", "4U2"), "#165DFF",2),
        T3("外拓区域", Lists.newArrayList("1O", "2C1", "3C2", "3C1", "4U1", "4C2", "5U2", "5U1"), "#37D4CF",3),
        T4("谨慎区域", Lists.newArrayList("2O", "3O", "4C1", "5C2", "6U2", "6U1"), "#5470C6",4),
        T5("禁入区域", Lists.newArrayList("4O", "5C1", "5O", "6C2", "6C1", "6O"), "#91CC75",5),
        ;

        private String desc;
        private List<String> codes;
        private String color;
        private Integer order;

        private static final Map<String, Label_6_6Enum> CODE_MAP = Arrays.stream(Label_6_6Enum.values())
                .flatMap(l -> l.getCodes().stream())
                .collect(Collectors.toMap(s -> s,
                        s1 -> Arrays.stream(Label_6_6Enum.values())
                                .filter(s -> s.getCodes().contains(s1))
                                .findFirst()
                                .orElseThrow(() -> new LFBizException("Label_6_6Enum，code不存在"))));


        private static final List<String> CODES = Arrays.stream(Label_6_6Enum.values())
                .flatMap(l -> l.getCodes().stream())
                .collect(Collectors.toList());

        public static Label_6_6Enum of(String code) {
            return CODE_MAP.get(code);
        }

        public static Label_6_6Enum ofDesc(String desc) {
            return Arrays.stream(Label_6_6Enum.values())
                    .filter(s -> StringUtils.equals(s.getDesc(), desc))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * 多个code组合找出最优的
         *
         * @param codes
         * @return
         */
        public static Label_6_6Enum bestOf(String codes) {
            return CommonConstant.SLASH_SPLITTER.splitToList(codes).stream()
                    .map(CODE_MAP::get)
                    .filter(Objects::nonNull)
                    .min(Comparator.comparingInt(Label_6_6Enum::getOrder))
                    .orElse(null);
        }

        /**
         * 多个code组合必须全部在枚举里
         *
         * @param codes
         * @return
         */
        public static List<Label_6_6Enum> allOf(String codes) {
            if (StringUtils.isBlank(codes)) {
                return null;
            }
            List<String> codeList = CommonConstant.SLASH_SPLITTER.splitToList(codes);
            List<Label_6_6Enum> list = codeList.stream()
                    .map(CODE_MAP::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return list.size() == codeList.size() ? list : null;
        }

        public static String firstCode(String codes) {
            List<String> codeList = CommonConstant.SLASH_SPLITTER.splitToList(codes);
            return CODES.stream()
                    .filter(codeList::contains)
                    .findFirst()
                    .orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum GreenStarEnum {
        BASE(1, "基本级"),
        L1(2, "一星"),
        L2(3, "二星"),
        L3(4, "三星"),
        ;
        private int code;

        private String desc;

        public static GreenStarEnum of(int code) {
            return Arrays.stream(GreenStarEnum.values()).filter(s -> s.getCode() == code).findFirst().orElse(null);
        }

        public static GreenStarEnum of(String desc) {
            return Arrays.stream(GreenStarEnum.values()).filter(s -> StringUtils.equals(desc, s.getDesc())).findFirst().orElse(null);
        }

    }

    @Getter
    @AllArgsConstructor
    public enum OptionTypeEnum {
        LONG_FOR(1, "龙湖操盘"),
        DEV_MARKETING(2, "研发/营销操盘"),
        DEV(3, "研发操盘"),
        MARKETING(4, "营销操盘"),
        NEITHER(5, "非操盘"),
        ;

        private int code;

        private String desc;

        public static OptionTypeEnum of(int code) {
            return Arrays.stream(OptionTypeEnum.values()).filter(s -> s.getCode() == code).findFirst().orElse(null);
        }

        public static OptionTypeEnum of(String desc) {
            return Arrays.stream(OptionTypeEnum.values()).filter(s -> StringUtils.equals(desc, s.getDesc())).findFirst().orElse(null);
        }

    }
}
