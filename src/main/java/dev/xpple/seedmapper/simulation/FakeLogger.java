package dev.xpple.seedmapper.simulation;

import dev.xpple.seedmapper.command.commands.SimulateCommand;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class FakeLogger implements Logger {

    private final Logger original;

    public FakeLogger(Logger original) {
        this.original = original;
    }

    private boolean shouldLog() {
        return SimulateCommand.currentServer != null;
    }

    @Override
    public boolean isErrorEnabled() {
        return this.shouldLog() && this.original.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.shouldLog() && this.original.isErrorEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.shouldLog() && this.original.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.shouldLog() && this.original.isWarnEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.shouldLog() && this.original.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.shouldLog() && this.original.isInfoEnabled(marker);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.shouldLog() && this.original.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.shouldLog() && this.original.isDebugEnabled(marker);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.shouldLog() && this.original.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.shouldLog() && this.original.isTraceEnabled(marker);
    }

    @Override
    public String getName() {
        return this.original.getName();
    }

    @Override
    public void trace(String s) {
        if (this.shouldLog()) {
            this.original.trace(s);
        }
    }

    @Override
    public void trace(String s, Object o) {
        if (this.shouldLog()) {
            this.original.trace(s, o);
        }
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.trace(s, o, o1);
        }
    }

    @Override
    public void trace(String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.trace(s, objects);
        }
    }

    @Override
    public void trace(String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.trace(s, throwable);
        }
    }

    @Override
    public void trace(Marker marker, String s) {
        if (this.shouldLog()) {
            this.original.trace(marker, s);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        if (this.shouldLog()) {
            this.original.trace(marker, s, o);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.trace(marker, s, o, o1);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.trace(marker, s, objects);
        }
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.trace(marker, s, throwable);
        }
    }

    @Override
    public void debug(String s) {
        if (this.shouldLog()) {
            this.original.debug(s);
        }
    }

    @Override
    public void debug(String s, Object o) {
        if (this.shouldLog()) {
            this.original.debug(s, o);
        }
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.debug(s, o, o1);
        }
    }

    @Override
    public void debug(String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.debug(s, objects);
        }
    }

    @Override
    public void debug(String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.debug(s, throwable);
        }
    }

    @Override
    public void debug(Marker marker, String s) {
        if (this.shouldLog()) {
            this.original.debug(marker, s);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        if (this.shouldLog()) {
            this.original.debug(marker, s, o);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.debug(marker, s, o, o1);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.debug(marker, s, objects);
        }
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.debug(marker, s, throwable);
        }
    }

    @Override
    public void info(String s) {
        if (this.shouldLog()) {
            this.original.info(s);
        }
    }

    @Override
    public void info(String s, Object o) {
        if (this.shouldLog()) {
            this.original.info(s, o);
        }
    }

    @Override
    public void info(String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.info(s, o, o1);
        }
    }

    @Override
    public void info(String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.info(s, objects);
        }
    }

    @Override
    public void info(String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.info(s, throwable);
        }
    }

    @Override
    public void info(Marker marker, String s) {
        if (this.shouldLog()) {
            this.original.info(marker, s);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        if (this.shouldLog()) {
            this.original.info(marker, s, o);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.info(marker, s, o, o1);
        }
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.info(marker, s, objects);
        }
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.info(marker, s, throwable);
        }
    }

    @Override
    public void warn(String s) {
        if (this.shouldLog()) {
            this.original.warn(s);
        }
    }

    @Override
    public void warn(String s, Object o) {
        if (this.shouldLog()) {
            this.original.warn(s, o);
        }
    }

    @Override
    public void warn(String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.warn(s, objects);
        }
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.warn(s, o, o1);
        }
    }

    @Override
    public void warn(String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.warn(s, throwable);
        }
    }

    @Override
    public void warn(Marker marker, String s) {
        if (this.shouldLog()) {
            this.original.warn(marker, s);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        if (this.shouldLog()) {
            this.original.warn(marker, s, o);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.warn(marker, s, o, o1);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.warn(marker, s, objects);
        }
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.warn(marker, s, throwable);
        }
    }

    @Override
    public void error(String s) {
        if (this.shouldLog()) {
            this.original.error(s);
        }
    }

    @Override
    public void error(String s, Object o) {
        if (this.shouldLog()) {
            this.original.error(s, o);
        }
    }

    @Override
    public void error(String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.error(s, o, o1);
        }
    }

    @Override
    public void error(String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.error(s, objects);
        }
    }

    @Override
    public void error(String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.error(s, throwable);
        }
    }

    @Override
    public void error(Marker marker, String s) {
        if (this.shouldLog()) {
            this.original.error(marker, s);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        if (this.shouldLog()) {
            this.original.error(marker, s, o);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        if (this.shouldLog()) {
            this.original.error(marker, s, o, o1);
        }
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        if (this.shouldLog()) {
            this.original.error(marker, s, objects);
        }
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        if (this.shouldLog()) {
            this.original.error(marker, s, throwable);
        }
    }
}
