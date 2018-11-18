package com.martonbot.dnem.filters;

import com.martonbot.dnem.data.Dnem;

public abstract class Filter {

    public abstract boolean evaluate(Dnem dnem);
}
