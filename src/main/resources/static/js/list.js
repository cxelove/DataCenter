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
        $('body').append('<table id = "' +key+ '" ></table>');
    }
    var tables = {};
    for (var key in cols) {
        tables[key] = layui.table;
        tables[key].render({
            elem: '#' + key
            , url: './api/getAllLatest?measure=' + key
            //  , toolbar: '#toolbarDemo'
            //  , height: 'full-20'
            , done: function (res, curr, count) {
                var ch = $('[lay-id=' + res['msg'] + '] td[data-field="obtime"]');
                var to = new Date();
                for (var i = 0; i < ch.length; i++) {
                    if (ch[i] == '---') {
                        $(ch[i]).parent().css('color', 'red');//设置css
                        return;
                    }
                    var from = new Date(ch[i].textContent.replace(/-/g, "/") + ":00");
                    if (((to - from) >= (5 * 60 * 1000)) && ((to - from) < (60 * 60 * 1000))) {
                        $(ch[i]).parent().css('color', 'orange');//设置css
                    } else if ((to - from) >= (60 * 60 * 1000)) {
                        $(ch[i]).parent().css('color', 'red');//设置css
                    }
                }
            }
            , cellMinWidth: 90 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
            , defaultToolbar: []
            , cols: eval("(" + cols[key] + ")")
        });
    }

    window.setInterval(function (args) {
        for (var key in cols) {
            tables[key].reload(key, {url: './api/getAllLatest?measure=' + key});
        }
    }, 30000);
});

