package org.dspace.content.dao;

import org.dspace.core.Context;


public abstract class AuthorDAO
{
    protected Context context;

    public AuthorDAO(){}

    protected AuthorDAO(Context ctx)
    {
        context = ctx;
    }
}
