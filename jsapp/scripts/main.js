'use strict';

$(function() {
    var chart = new Highcharts.Chart({
        chart: {
            renderTo: "channel-visitors",
            type: 'spline'
        },
        title: {
            text: "Channel visitors"
        },
        xAxis: {
            type: 'datetime',
            title: {
                text: 'Time'
            }
        },
        yAxis: {
            title: {
                text: 'Visitors'
            },
            min: 0
        },
        series: []
    });

    var meterdata = new Rx.Subject();
    var ws = new WebSocket('ws://' + location.host + '/ws');
    ws.onmessage = function(event) {
        meterdata.onNext(JSON.parse(event.data));
        console.log(event.data);
    };

    var visitors = meterdata.filter(function(data) {
        return data['meter-id'].startsWith('channel-nicks');
    }).map(function(data) {
        return {
            place: data['meter-id'].split('/').slice(1).join('/'),
            time: Date.parse(data['time']),
            people: data['value'],
            peopleCount: data['value'].length
        }
    });

    visitors.subscribe(function(data) {
        var serie = chart.series.find(function(s) { return s.name === data.place; })
        var point = [data.time, data.peopleCount];
        if (serie) {
            serie.addPoint(point);
        } else {
            chart.addSeries({
                name: data.place,
                data: [point]
            });
        }
    });
});