package com.huangruihao.canteen.strategy;

/**
 * Created by alexwang on 6/6/16.
 */
public class DormitoryNearbyStrategy extends Strategy {
    @Override
    public Object[] getCanteensWithProbability() {
        return new Object[] {
                TAOLI_B1,      3,
                TAOLI_1,      10,
                TAOLI_2,       1,
                TAOLI_3,       1,
                ZIJING_B1,     1,
                ZIJING_1,     10,
                ZIJING_2,      1,
                ZIJING_3,      1,
                ZIJING_4,     10,
        };
    }
}
