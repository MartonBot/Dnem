package com.martonbot.dnem.filters;

import com.martonbot.dnem.DnemActivity;

/**
 * Created by mgrihangne on 16/10/2017.
 */

public class IsUndoneFilter extends DnemFilter {

    @Override
    boolean evaluate(DnemActivity activity) {
        return !activity.isDoneForToday();
    }
}
