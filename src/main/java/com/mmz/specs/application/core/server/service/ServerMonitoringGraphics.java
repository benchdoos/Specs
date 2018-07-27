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
    private boolean cpuSystemShow = true;
    private boolean temperatureServerShow = true;
    private boolean cpuServerShow = true;
    private boolean ramServerShow = true;
    private boolean usersShow = true;
    private boolean ramSystemShow = true;

    private ArrayList<Float> xAges = ServerMonitoringBackgroundService.getInstance().getGraphicXAges();
    private XYChart chart = new XYChartBuilder().width(0).height(0).yAxisTitle("Нагрузка (%)").theme(Styler.ChartTheme.Matlab).build();


    @SuppressWarnings("SuspiciousNameCombination")
    public XYChart getChart(int width, int height) {
        final Color BACKGROUND_COLOR = new JPanel().getBackground();
        Font defaultFont = new JLabel().getFont();

        chart = new XYChartBuilder().width(width).height(height).yAxisTitle("Нагрузка (%)").theme(Styler.ChartTheme.Matlab).build();

        initStyle();

        initBackgroundColor(BACKGROUND_COLOR);

        initFont(defaultFont);


        ArrayList<Float> serverMemoryData = ServerMonitoringBackgroundService.getInstance().getServerMemoryLoadValues();

        ArrayList<Float> systemMemoryData = ServerMonitoringBackgroundService.getInstance().getSystemMemoryLoadValues();

        ArrayList<Float> cpuData = ServerMonitoringBackgroundService.getInstance().getCpuLoadValues();

        ArrayList<Float> cpuServerData = ServerMonitoringBackgroundService.getInstance().getCpuLoadByServerValues();

        ArrayList<Float> cpuTemperatureData = ServerMonitoringBackgroundService.getInstance().getCpuTemperatureValues();

        ArrayList<Float> usersCountData = ServerMonitoringBackgroundService.getInstance().getUsersConnectedValues();


        initSystemRamGraphics(systemMemoryData);

        initServerRamGraphics(serverMemoryData);

        initCpuServerGraphics(cpuServerData);

        initTemperatureCpuGraphics(cpuTemperatureData);

        initUsersGraphics(usersCountData);

        initCpuSystemGraphics(cpuData);

        return chart;
    }

    private void initCpuSystemGraphics(ArrayList<Float> cpuData) {
        if (cpuSystemShow) {
            XYSeries cpuSystem = chart.addSeries("ЦП (система)", xAges, cpuData);
            cpuSystem.setMarker(SeriesMarkers.NONE).setLineColor(Color.RED);
            cpuSystem.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void initUsersGraphics(ArrayList<Float> usersCountData) {
        if (usersShow) {
            XYSeries users = chart.addSeries("Пользователи", xAges, usersCountData);
            users.setMarker(SeriesMarkers.NONE).setLineColor(Color.DARK_GRAY);
            users.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void initTemperatureCpuGraphics(ArrayList<Float> cpuTemperatureData) {
        if (temperatureServerShow) {
            String DEGREE = "\u00b0";
            XYSeries temperature = chart.addSeries(DEGREE + "C ЦП", xAges, cpuTemperatureData);
            temperature.setMarker(SeriesMarkers.NONE).setLineColor(Color.ORANGE);
            temperature.setLineStyle(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void initCpuServerGraphics(ArrayList<Float> cpuServerData) {
        if (cpuServerShow) {
            XYSeries cpuServer = chart.addSeries("ЦП (сервер)", xAges, cpuServerData);
            cpuServer.setMarker(SeriesMarkers.NONE).setLineColor(Color.BLUE);
            cpuServer.setLineStyle(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void initServerRamGraphics(ArrayList<Float> serverMemoryData) {
        if (ramServerShow) {
            XYSeries serverMemory = chart.addSeries("ОЗУ (сервер)", xAges, serverMemoryData);
            serverMemory.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
            serverMemory.setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN.darker());
            serverMemory.setFillColor(Color.GREEN.darker());
        }
    }

    private void initSystemRamGraphics(ArrayList<Float> systemMemoryData) {
        if (ramSystemShow) {
            XYSeries systemMemory = chart.addSeries("ОЗУ (система)", xAges, systemMemoryData);
            systemMemory.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
            systemMemory.setMarker(SeriesMarkers.NONE).setLineColor(new Color(239, 248, 255));
            systemMemory.setFillColor(new Color(239, 248, 255));
        }
    }

    private void initStyle() {
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
    }

    private void initBackgroundColor(Color BACKGROUND_COLOR) {
        chart.getStyler().setChartBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setLegendBackgroundColor(BACKGROUND_COLOR);
        chart.getStyler().setPlotBackgroundColor(BACKGROUND_COLOR);
    }

    private void initFont(Font defaultFont) {
        chart.getStyler().setLegendFont(defaultFont);
        chart.getStyler().setBaseFont(defaultFont);
        chart.getStyler().setAnnotationsFont(defaultFont);
        chart.getStyler().setAxisTitleFont(defaultFont);
        chart.getStyler().setChartTitleFont(defaultFont);
        chart.getStyler().setAxisTickLabelsFont(defaultFont);
    }

    public void setRamSystemShow(boolean ramSystemShow) {
        this.ramSystemShow = ramSystemShow;
    }

    public void setCpuSystemShow(boolean cpuSystemShow) {
        this.cpuSystemShow = cpuSystemShow;
    }

    public void setTemperatureServerShow(boolean temperatureServerShow) {
        this.temperatureServerShow = temperatureServerShow;
    }

    public void setCpuServerShow(boolean cpuServerShow) {
        this.cpuServerShow = cpuServerShow;
    }

    public void setRamServerShow(boolean ramServerShow) {
        this.ramServerShow = ramServerShow;
    }

    public void setUsersShow(boolean usersShow) {
        this.usersShow = usersShow;
    }
}
