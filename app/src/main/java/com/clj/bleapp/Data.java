package com.clj.bleapp;

public class Data {


    // Okay, full acknowledgment that public members are not a good idea, however
    // this is a Spinner demo not an exercise in java best practices.
    public int id = 0;
    public String name = "";

    // A simple constructor for populating our member variables for this tutorial.
    public Data(int _id, String _name) {
        id = _id;
        name = _name;
    }

    // The toString method is extremely important to making this class work with a Spinner
    // (or ListView) object because this is the method called when it is trying to represent
    // this object within the control.  If you do not have a toString() method, you WILL
    // get an exception.
    public String toString() {
        return (name);
    }

    public void setDataId(int i) {
    }

    public void setData(String s) {
    }


}
