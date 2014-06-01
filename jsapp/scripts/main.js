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

    var ViewModel = function() {
        var self = this;
        self.followtags = ko.observableArray();
        self.selectTag = function (tag) {
            console.log(tag);
            ko.utils.arrayForEach(self.followtags(), function(tag) { tag.isVisible(false); });
            tag.isVisible(true);
        };
    };
    var viewModel = new ViewModel()
    ko.applyBindings(viewModel);

    var meterdata = new Rx.Subject();
    var ws = new WebSocket('ws://' + location.host + '/ws');
    ws.onmessage = function(event) {
        meterdata.onNext(JSON.parse(event.data));
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

    var tags = meterdata.filter(function(data) {
        return data['meter-id'].startsWith('contains-text');
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

    tags.subscribe(function(data) {
        var tagText = data['meter-id'].split('/').slice(1).join('');
        var tagData = ko.utils.arrayFirst(viewModel.followtags(), function(t) { return t.tagname === tagText; })
        var message = {person: data.value.sender.nick, text: data.value.text};
        if (tagData) {
            tagData.messages.push(message);
        } else {
            viewModel.followtags.push({
                tagname: tagText,
                isVisible: ko.observable(false),
                messages: ko.observableArray([message])
            });
        }
    });
});