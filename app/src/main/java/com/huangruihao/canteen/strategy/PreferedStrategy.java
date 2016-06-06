package com.huangruihao.canteen.strategy;

/**
 * Created by alexwang on 6/6/16.
 */
public class PreferedStrategy extends RandomStrategy {
    public Object[] getCanteensWithProbability() {
        return new Object[] {
                TAOLI_B1,      1,
                TAOLI_1,       10,
                TAOLI_2,       1,
                TAOLI_3,       1,
                ZIJING_B1,     1,
                ZIJING_1,     10,
                ZIJING_2,      1,
                ZIJING_3,      1,
                ZIJING_4,     10,
                ZHILAN_1,      1,
                ZHILAN_2,      1,
                YUSHU_1,       1,
                YUSHU_2,       1,
                GUANCHOU_1,    1,
                GUANCHOU_2,    1,
                QINGFEN_1,     1,
                QINGFEN_2,     1,
                HEYUAN,        1,
                QINGQING,      1
        };
    }
}
