/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.application.core.server.service;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ServerMonitoringGraphics {
    private final static Color BACKGROUND_COLOR = new Color(242, 242, 242);
    private final static Font defaultFont = new JLabel().getFont();
    private final String DEGREE = "\u00b0";

    @SuppressWarnings("SuspiciousNameCombination")
    public XYChart getChart(int width, int height) {
        final Color BACKGROUND_COLOR = new Color(242, 242, 242);
        Font defaultFont = new JLabel().getFont();

        // Create Chart
        XYChart chart = new XYChartBuilder().width(width).height(height).yAxisTitle("Нагрузка (%)").theme(Styler.ChartTheme.Matlab).build();

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendSeriesLineLength(12);
        chart.getStyler().setLegendPadding(5);
        chart.getStyler().setLegendBorderColor(new Color(1f, 0f, 0f, 0f));

        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        chart.getStyler().setYAxisDecimalPattern("###.##");

        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(1);


        chart.getStyler().setChartBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setLegendBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setPlotBackgroundColor(BACKGROUND_COLOR);

        chart.getStyler().setLegendFont(defaultFont);
        chart.getStyler().setBaseFont(defaultFont);
        chart.getStyler().setAnnotationsFont(defaultFont);
        chart.getStyler().setAxisTitleFont(defaultFont);
        chart.getStyler().setChartTitleFont(defaultFont);
        chart.getStyler().setAxisTickLabelsFont(defaultFont);

        // Series
        // @formatter:off

        ArrayList<Float> xAges = ServerMonitoringBackgroundService.getInstance().getGraphicXAges();

        ArrayList<Float> memoryData = ServerMonitoringBackgroundService.getInstance().getMemoryLoadValues();

        ArrayList<Float> cpuData = ServerMonitoringBackgroundService.getInstance().getCpuLoadValues();

        ArrayList<Float> cpuServerData = ServerMonitoringBackgroundService.getInstance().getCpuLoadByServerValues();

        ArrayList<Float> cpuTemperatureData = ServerMonitoringBackgroundService.getInstance().getCpuTemperatureValue();

        XYSeries memory = chart.addSeries("ОЗУ (cервер)", xAges, memoryData);
        memory.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        memory.setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN.darker());
        memory.setFillColor(Color.GREEN.darker());

        XYSeries cpuServer = chart.addSeries("ЦП (сервер)", xAges, cpuServerData);
        cpuServer.setMarker(SeriesMarkers.NONE).setLineColor(Color.BLUE);
        cpuServer.setLineStyle(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        XYSeries temperature = chart.addSeries(DEGREE + "C ЦП", xAges, cpuTemperatureData);
        temperature.setMarker(SeriesMarkers.NONE).setLineColor(Color.ORANGE);
        temperature.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        XYSeries cpuSystem = chart.addSeries("ЦП (система)", xAges, cpuData);
        cpuSystem.setMarker(SeriesMarkers.NONE).setLineColor(Color.RED);
        cpuSystem.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        return chart;
    }
}
