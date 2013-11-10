package com.ts.timeseries.matrix;

import com.ts.timeseries.data.SimpleTimeSeries;
import com.ts.timeseries.data.TimeSeries;
import com.ts.timeseries.hdf.DataWriter;
import com.ts.timeseries.util.Preconditions;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.*;


final class SimpleMatrix implements Matrix {

    SimpleMatrix(SortedSet<Long> grid, Set<String> symbols) {
        int col = 0;
        for (String symbol : symbols)
            columnNames.put(symbol, col++);

        for (Long time : grid)
            data.put(time, new SimpleRow(this.columnNames.size()));
    }

    private final SortedMap<Long, SimpleRow> data = new TreeMap<Long, SimpleRow>();
    private final Map<String, Integer> columnNames = new HashMap<String, Integer>();

    SimpleMatrix(Set<String> symbols) {
        int col = 0;
        for (String symbol : symbols)
            columnNames.put(symbol, col++);
    }

    @Override
    public double readScalar(String name, Long time) {
        Preconditions.checkArgument(this.columnNames.keySet().contains(name), "Unknown column: " + name);
        Preconditions.checkArgument(this.data.containsKey(time), "Unknown timestamp: " + time);

        final SimpleRow row = data.get(time);
        final int index = this.columnNames.get(name);
        return row.values[index];
    }

    @Override
    public void writeScalar(String name, Long time, double scalar) {
        Preconditions.checkArgument(this.columnNames.keySet().contains(name), "Unknown column: " + name);

        SimpleRow row = data.get(time);
        if (row == null)
        {
            this.data.put(time, new SimpleRow(this.columnNames.size()));
            row = data.get(time);
        }

        int index = this.columnNames.get(name);
        row.values[index] = scalar;
    }

    @Override
    public SortedMap<String, Double> readVector(Long time) {
        Preconditions.checkArgument(this.data.containsKey(time), "Unknown timestamp: " + time);
        SortedMap<String, Double> x = new TreeMap<String, Double>();
        SimpleRow row = this.data.get(time);

        for (String symbol : this.columnNames.keySet())
            x.put(symbol, row.values[columnNames.get(symbol)]);

        return x;
    }

    @Override
    public void writeVector(Long time, Map<String, Double> data) {
        //Preconditions.checkArgument(this.data.containsKey(time), "Unknown timestamp: " + time);
        SimpleRow row = this.data.get(time);

        if (row == null)
        {
            this.data.put(time, new SimpleRow(this.columnNames.size()));
            row = this.data.get(time);
        }

        for (String symbol : data.keySet())
        {
            Preconditions.checkArgument(this.columnNames.keySet().contains(symbol), "Unknown column: " + symbol);
            row.values[columnNames.get(symbol)] =  data.get(symbol);
        }
    }

    @Override
    public Set<String> columns() {
        return this.columnNames.keySet();
    }

    @Override
    public void setTimeSeries(String columnName, TimeSeries timeSeries) {
        for (Map.Entry<Long, Double> entry : timeSeries.points().entrySet())
            this.writeScalar(columnName, entry.getKey(), entry.getValue());
    }

    @Override
    public TimeSeries getTimeSeries(String columnName) {
        Preconditions.checkArgument(this.columnNames.keySet().contains(columnName), "Unknown column: " + columnName);
        final int index = this.columnNames.get(columnName);
        final SortedMap<Long, Double> data = new TreeMap<Long, Double>();

        for (Map.Entry<Long, SimpleRow> entry : this.data.entrySet()) {
            data.put(entry.getKey(), entry.getValue().values[index]);
        }

        return new SimpleTimeSeries(data);
    }

    @Override
    public SortedSet<Long> timegrid() {
        return new TreeSet<Long>(data.keySet());
    }

    @Override
    public void to_hdf(File file) {
        DataWriter writer = new DataWriter(file);
        writer.createGroup("data");
        for (String name : this.columnNames.keySet())
            try {
                writer.writeSeries("data", name, this.getTimeSeries(name));
            } catch (HDF5JavaException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        writer.close();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleMatrix that = (SimpleMatrix) o;

        if (!that.columnNames.keySet().equals(this.columnNames.keySet())) return false;
        if (!that.data.keySet().equals(this.data.keySet())) return false;

        for (String column : this.columnNames.keySet())
            if (!that.getTimeSeries(column).equals(this.getTimeSeries(column)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (columnNames != null ? columnNames.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()

    {
        String s="";
        for (String key : this.columnNames.keySet())
        {
            s+=this.columnNames.get(key) + ": " + key + "\n";
        }

        for (Long time : this.data.keySet())
        {
            s += time + " " + this.readVector(time).toString() + "\n";
        }

        return s;
    }
}