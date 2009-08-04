import groovy.beans.Bindable

@Bindable class PrefsPanelModel {
    String location
    boolean isCelsius
    boolean isFahrenheit

    void elementInit(Map args) {
        location = app.models.WeatherWidget.location
        isCelsius = app.models.WeatherWidget.celsius
        isFahrenheit = !isCelsius
    }

}