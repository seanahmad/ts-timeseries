package com.ts.timeseries.matrix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;


public class MatrixBuilderTest {

    private Matrix A;

    @Before
    public void setUp() throws Exception {
        A = MatrixBuilder.build(ImmutableList.of("Peter", "Maffay"), ImmutableSortedSet.of(0L, 1L, 5L, 3L));
        A.writeScalar("Peter", 3L, 1.0);
        A.writeScalar("Peter", 5L, 5.0);
        A.writeScalar("Maffay", 1L, 1.0);
        A.writeScalar("Maffay", 3L, 3.0);
    }

    @Test(expected = InvocationTargetException.class)
    public void testConstruction() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        final Constructor constructor = MatrixBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testMatrix() {
        Assert.assertEquals(A.getTimeSeries("Peter").points().keySet().size(), 4);
        Assert.assertEquals(A.getTimeSeries("Maffay").points().keySet().size(), 4);
    }


    @Test
    public void testBuild() throws Exception {
        Assert.assertEquals(A.columns(), ImmutableSortedSet.of("Maffay", "Peter"));
        Assert.assertEquals(A.timegrid(), ImmutableSortedSet.of(0L,1L,3L,5L));
    }

    @Test
    public void testWrite() throws Exception {
        Matrix mat = MatrixBuilder.build(ImmutableList.of("A", "B"), ImmutableSortedSet.of(10L, 20L));
        mat.writeVector(15L, ImmutableMap.of("A",2.0,"B",3.0));
    }
}
