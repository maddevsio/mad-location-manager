package com.example.mlmexample.sensors;

public abstract class ISensor {
    private boolean m_isActive = false;

    public final boolean start() {
        if (onStart()) {
            m_isActive = true;
            return true;
        }
        return false;
    }

    public final boolean stop() {
        if (onStop()) {
            m_isActive = false;
            return true;
        }
        return false;
    }

    public boolean is_active() {
        return m_isActive;
    }

    // Subclasses implement their own start/stop logic here
    protected abstract boolean onStart();
    protected abstract boolean onStop();
}