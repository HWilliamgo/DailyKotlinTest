package polyv_pre_env_test;

import static polyv_pre_env_test.Mode.*;

/**
 * date: 2019/12/17
 * author: hwj
 * description:
 */
public class Param {
    //debug,regression,preRelease,release
    static final String PARAM_MODE = "-mode";
    //true,false
    static final String PARAM_CLEAR_USER = "-clearUser";
    //param
    static Mode MODE = Mode.PRE_RELEASE;
    static String CLEAR_USER = "false";

    static Mode setMode(String mode) {
        if (mode == null) {
            return MODE;
        }
        switch (mode) {
            case "debug":
                MODE = Mode.DEBUG;
                break;
            case "regression":
                MODE = Mode.REGRESSION;
                break;
            case "preRelease":
                MODE = Mode.PRE_RELEASE;
                break;
            case "release":
                MODE = Mode.RELEASE;
                break;
        }
        return MODE;
    }

    static String setClearUser(String clearUser) {
        if (clearUser == null) {
            return CLEAR_USER;
        }
        if ("true".equals(clearUser)) {
            CLEAR_USER = clearUser;
        } else if ("false".equals(clearUser)) {
            CLEAR_USER = clearUser;
        }
        return CLEAR_USER;
    }
}
