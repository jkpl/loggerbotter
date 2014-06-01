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

    var tagTabs = $('#tag-tabs')
    var tagArea = $('#tag-area');
    var tagElements = {};

    function createTagContainer(tagString) {
        var container = $('<div></div>').addClass('list-group').hide();
        var tab = $('<li></li>');
        var link = $('<a href="#"></a>').text(tagString).click(function(e){
            showTag(tagString);
        });
        tab.append(link);
        tagTabs.append(tab);
        tagArea.append(container);
        tagElements[tagString] = {
            container: container,
            tab: tab
        }
    }

    function showTag(tagString) {
        $.each(tagElements, function(k, element) {
            element.container.hide();
            element.tab.removeClass('active');
        });
        var tagElement = tagElements[tagString];
        tagElement.container.show();
        tagElement.tab.addClass('active');
    }

    function addDataToTag(tagString, data) {
        var heading = $('<h5></h5>')
            .addClass('list-group-item-heading')
            .text(data.heading);
        var body = $('<p></p>')
            .addClass('list-group-item-text')
            .text(data.text);
        var groupItem = $('<div></div>').addClass('list-group-item')
            .append(heading).append(body);
        tagElements[tagString].container.prepend(groupItem);
    }

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
        var data = {heading: data.value.sender.nick, text: data.value.text};
        if (!tagElements[tagText]) {
            createTagContainer(tagText);
        }
        addDataToTag(tagText, data);
    });
});