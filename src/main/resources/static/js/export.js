layui.use(['jquery', 'layer', 'laydate', 'table', 'element'], function () {
    var table = layui.table;
    var laydate = layui.laydate;
    table.render({
        elem: '#table'
        , url: './api/getOnedayLimit?'
        , height: 'full-20'
        , loading: true
        , done: function (res, curr, count) {
            laydate.render({
                elem: '#date' //指定元素
            });
        }
        , toolbar: '#toolbarDemo'
        , cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
        , page: //true
            {
                limit: 60,
            }
        , cols: cols
    });
    table.on('toolbar(export)', function (obj) {
            switch (obj.event) {
                case 'query':
                    if ($('#stationid').val() && $('#date').val()) {
                        var stationid = $('#stationid').val();
                        var date = $('#date').val();
                        table.reload('table',{url:'./api/getOnedayLimit?stationId=' + stationid + '&date=' + date,});
                        $('#stationid').val(stationid);
                        $('#date').val(date);
                    }
                    break;
                case 'export' :
                    if ($('#stationid').val() == '' || $('#date').val() == '')
                        return;
                    window.location.href = 'export/download?stationId=' + $('#stationid').val() + '&date=' + $('#date').val();
                    break;
            }
        }
    )

});

