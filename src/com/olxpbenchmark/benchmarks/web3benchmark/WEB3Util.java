/*
 * Copyright 2023 by Web3Bench Project
 * This work was based on the OLxPBench Project

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */


/*
 * Copyright 2021 OLxPBench
 * This work was based on the OLTPBenchmark Project

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */


package com.olxpbenchmark.benchmarks.web3benchmark;

import com.olxpbenchmark.util.RandomGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static com.olxpbenchmark.benchmarks.web3benchmark.WEB3Config.*;

public class WEB3Util {

    private static final RandomGenerator ran = new RandomGenerator(0);

    // public static String randomStr(int strLen) {
    //     if (strLen > 1)
    //         return ran.astring(strLen - 1, strLen - 1);
    //     else
    //         return "";
    // }

    public static String randomStr(int strLen) {
        if (strLen <= 0) {
            return "";
        }

        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(strLen);

        for (int i = 0; i < strLen; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static String randomNStr(int stringLength) {
        if (stringLength > 0)
            return ran.nstring(stringLength, stringLength);
        else
            return "";
    }

    public static String randomHexString(int stringLength) {
        if (stringLength > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stringLength - 2; i++) {
                sb.append(Integer.toHexString(ran.number(0, 15)));
            }
            return "0x" + sb.toString();
        } else {
            return "";
        }
    }

    public static String randomHashString() {
        return randomHexString(WEB3Config.configHashLength);
    }

    public static String randomAddressString() {
        return randomHexString(WEB3Config.configAddressLength);
    }

    public static String convertToHexString(long x, int length) {
        String hexString = Long.toHexString(x);
        if (hexString.length() < length - 2) {
            StringBuilder padding = new StringBuilder();
            int paddingLength = length - 2 - hexString.length();
            for (int i = 0; i < paddingLength; i++) {
                padding.append("0");
            }
            hexString = padding.toString() + hexString;
        }
        return "0x" + hexString;
    }

    public static String convertToHexString(long index, int length, String type) {
        String hexString = Long.toHexString(index);
        if (hexString.length() < length - 2 - type.length()) {
            StringBuilder padding = new StringBuilder();
            int paddingLength = length - 2 - hexString.length() - type.length();
            for (int i = 0; i < paddingLength; i++) {
                padding.append("0");
            }
            hexString = padding.toString() + hexString;
        } else {
            return "0x" + hexString.substring(0, length - 2 - type.length()) + type;
        }
        return "0x" + hexString + type;
    }

    // public static String convertToHashString(long index) {
    //     return convertToHexString(index, WEB3Config.configHashLength);
    // }

    public static String convertToBlockHashString(long index) {
        return convertToHexString(index, WEB3Config.configHashLength, "block");
    }

    public static String convertToTxnHashString(long index) {
        return convertToHexString(index, WEB3Config.configHashLength, "txn");
    }

    public static String convertToAddressString(long index) {
        return convertToHexString(index, WEB3Config.configAddressLength, "address");
    }

    public static String convertToTokenAddressString(long index) {
        return convertToHexString(index, WEB3Config.configAddressLength, "token");
    }

    // public static String convertToTxnFromAddressString(long index) {
    //     return convertToHexString(index, WEB3Config.configAddressLength, "txnFrom");
    // }

    public static String convertToContractAddressString(long index) {
        return convertToHexString(index, WEB3Config.configAddressLength, "contract");
    }

    // public static String getCurrentTime() {
    //     return dateFormat.format(new java.util.Date());
    // }

    // public static String formattedDouble(double d) {
    //     String dS = "" + d;
    //     return dS.length() > 6 ? dS.substring(0, 6) : dS;
    // }

    public static long randomNumber(long min, long max, Random r) {
        return (long) (r.nextDouble() * (max - min + 1) + min);
    }

    public static int randomNumber(int min, int max, Random r) {
        return (int) (r.nextDouble() * (max - min + 1) + min);
    }

    // public static int nonUniformRandom(int A, int C, int min, int max, Random r) {
    //     return (((randomNumber(0, A, r) | randomNumber(min, max, r)) + C) % (max
    //             - min + 1))
    //             + min;
    // }

    public static long getTimestamp(long block_number) {
        return WEB3Config.configBaseTimestamp + block_number * WEB3Config.configTimestampInterval;
    }

    public static long getGasUsed(long block_number) {
        return (block_number + WEB3Config.configGasLimitLowerBound / 10) % WEB3Config.configGasLimitLowerBound;
    }

    public static long getTransactionCount(long block_number) {
        return WEB3Config.configTxnPerBlock;
    }

}
