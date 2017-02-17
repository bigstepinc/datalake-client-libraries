package com.bigstep.datalake;

/**
 * Created by alex on 11/23/16.
 */
public class IntegerSchema implements java.io.Serializable
{
    private Integer val;

    public int getVal(){ return val; }
    public void setVal(Integer val){ this.val = val; }
}