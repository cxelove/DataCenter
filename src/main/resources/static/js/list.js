layui.use(['jquery', 'layer', 'table', 'element'], function () {
    var $ = layui.jquery,
        layer = layui.layer;

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
                    if(ch[i]=='---'){
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

    //   var  table = layui.table
    //
    // table.render({
    //     elem: '#datatable'
    //     , url: 'ajax/getAllLatest.do'
    //     , toolbar: '#toolbarDemo'
    //     , height: 'full-20'
    //     , done: function (res, curr, count) {
    //         var ch = $('.layui-table-body td[data-field="OBTIME"]');
    //         var to = new Date();
    //         for (var i = 0; i < ch.length; i++) {
    //             var from = new Date(ch[i].textContent.replace(/-/g, "/") + ":00");
    //             if (((to - from) >= (5 * 60 * 1000)) && ((to - from) < (60 * 60 * 1000))) {
    //                 $(ch[i]).parent().css('color', 'orange');//设置css
    //             } else if ((to - from) >= (60 * 60 * 1000)) {
    //                 $(ch[i]).parent().css('color', 'red');//设置css
    //             }
    //         }
    //         if(flag == 0){
    //         	flag = 1;
    //         	 window.setInterval(function (args) {
    //                  table.reload('datatable', {url: 'ajax/getAllLatest.do' });
    //              }, 30000);
    //         }
    //     }
    //     , cellMinWidth: 90 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
    //    , defaultToolbar: []
    //     , cols: cols
    // });
});

