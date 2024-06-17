package com.longfor.plm.project.dto.board.response;

import com.longfor.plm.project.enums.C1IndexGroupEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lilei
 * @description: TODO
 * @date : 2022/7/4 15:57
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class C1DataDashboardResponse {

    private int total;

    private int itemTotal;

    private List<Level> items;

    private transient String levelTmp;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Level {

        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;

        /**
         * 建面
         */
        private BigDecimal buildingAreaAvg;

        /**
         * 容积率
         */
        private BigDecimal slotRateAvg;

        /**
         * 户均面积
         */
        private BigDecimal houseAreaAvg;

        /**
         * 6*6标签
         */
        private List<Label6_6> label6_6List;

        /**
         * 6*6标签分级
         */
        private List<Label6_6> group6_6List;

        /**
         * 业态
         */
        private List<Formats> formatsList;

        /**
         * 建面
         */
        private List<BuildingArea> buildingAreaList;

        /**
         * 容积
         */
        private List<SlotRate> slotRateList;

        /**
         * 项目
         */
        private List<PhaseInfo> phaseInfoList;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Label6_6 {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;

        /**
         * 能级
         */
        private String group;


        /**
         * 能级颜色
         */
        private String rgba;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Formats {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;

        public int orderByPrefix() {
            return C1IndexGroupEnum.HouseFormatEnum.ofPrefix(this.code).getOrder();
        }

        public int orderByLength() {
            return this.code.length();
        }

    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class BuildingArea {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;

    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SlotRate {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;

    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ConfigPLevel {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ConfigDLevel {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ConfigSLevel {
        /**
         * code
         */
        private String code;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 占比
         */
        private BigDecimal percent;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class PhaseInfo {
        /**
         * 地区公司
         */
        private String company;

        /**
         * 城市
         */
        private String city;

        /**
         * 项目分期
         */
        private String phaseName;
    }

}
