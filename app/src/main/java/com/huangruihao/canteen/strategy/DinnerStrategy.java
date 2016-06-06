package com.huangruihao.canteen.strategy;

/**
 * Created by alexwang on 6/6/16.
 */
public class DinnerStrategy extends Strategy {
    public Object[] getCanteensWithProbability() {
        return new Object[] {
                TAOLI_B1,      1,
                ZIJING_B1,     3,
                ZHILAN_1,      5,
                YUSHU_1,       5,
                YUSHU_2,       2,
                HEYUAN,        3
        };
    }
}
