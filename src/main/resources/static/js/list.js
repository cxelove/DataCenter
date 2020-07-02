layui.use(['jquery', 'layer', 'table', 'element'], function () {
    var $ = layui.jquery,
        layer = layui.layer;
    var listtable = localStorage.getItem("listtable");
    if (listtable == null) {
        listtable = $.ajax({url: './api/getListTable', async: false}).responseText;
        localStorage.setItem("listtable", listtable);
    }
    var cols = $.parseJSON(listtable);
    for (var key in cols) {
        //添加html标签
        $('body').append('<table id = "' +key+ '" lay-filter="'+key+'" ></table>');
    }
    var tables = {};
    for (var key in cols) {
        tables[key] = layui.table;
        tables[key].render({
            elem: '#' + key
            , url: './api/getAllLatest?measure=' + key
            ,initSort: {
                field: 'STATIONID' //排序字段，对应 cols 设定的各字段名
                ,type: 'asc' //排序方式  asc: 升序、desc: 降序、null: 默认排序
            }
            , done: function (res, curr, count) {
                // var ch = $('[lay-id=' + res['msg'] + '] td[data-field="OBTIME"]');
                // var to = new Date();
                // for (var i = 0; i < ch.length; i++) {
                //     if (ch[i] == '---') {
                //         $(ch[i]).parent().css('color', 'red');//设置css
                //         return;
                //     }
                //     var from = new Date(ch[i].textContent.replace(/-/g, "/") + ":00");
                //     if (((to - from) >= (5 * 60 * 1000)) && ((to - from) < (60 * 60 * 1000))) {
                //         $(ch[i]).parent().css('color', 'orange');//设置css
                //     } else if ((to - from) >= (60 * 60 * 1000)) {
                //         $(ch[i]).parent().css('color', 'red');//设置css
                //     }
                // }
            }
            , cellMinWidth: 90 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
            , defaultToolbar: []
            , cols: eval("(" + cols[key] + ")")
        });
        tables[key].on('rowDouble('+key+')', function(obj){
            var index=top.layer.open({
                type: 2,
                title: '数据查询导出【'+obj.data["STATIONID"]+'】',
                shadeClose: true,
                shade: false,
                area: [window.top.innerWidth+"px",window.top.innerHeight+"px"],
                content:['export?stationid='+obj.data["STATIONID"]+'&date='+obj.data["OBTIME"],'no']
            });
        });
    }
    window.setInterval(function (args) {
        for (var key in cols) {
            tables[key].reload(key, {url: './api/getAllLatest?measure=' + key});
        }
    }, 30000);
});

