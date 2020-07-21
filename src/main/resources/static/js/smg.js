layui.use(['table', 'layer', 'element'], function () {

    var table = layui.table,
        layer = layui.layer;

    var mylayer;
  //  table = $.extend(table, {config: {checkName: 'noRealTime'}});
    table.render({
        elem: '#datatable'
        // , toolbar: '#toolbarDemo'
        //  , defaultToolbar: ['']
        , url: './api/getStations'
        , initSort: {
            field: 'protocol' //排序字段，对应 cols 设定的各字段名
            , type: 'asc' //排序方式  asc: 升序、desc: 降序、null: 默认排序
        }
        , cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
        , cols: [[
            {field: 'stationid', width: 80, title: '站点号'}
            , {field: 'alias', title: '名称', edit: 'text'}
            , {
                field: 'obtime', width: 150, title: '最后到包时间'
                , templet: function (d) {
                    return new Date(d["obtime"]).Format("yyyy-MM-dd hh:mm");
                }
            }
            , {field: 'protocol', width: 140, title: '协议'}
            , {field: 'lng', width: 100, title: '经度', edit: 'text'}
            , {field: 'lat', width: 100, title: '纬度', edit: 'text'}
            // , {field: 'noRealTime', width: 40, title: '实时?',type:'checkbox'}
            , {fixed: 'right', width: 240, title: "操作", align: 'center', toolbar: '#tablebtn', unresize: true}
        ]]
        , done: function (res, page, count) {
           // console.log(res);
        }
    });

    /**
     * 远程升级
     * @param layer
     * @param data
     */
    function openupdate(layer, data) {
        mylayer = top.layer.open({
            type: 2,
            title: "升级[" + data['stationid'] + "]",
            shadeClose: true,
            shade: false,
            maxmin: false, //开启最大化最小化按钮
            area: ['680px', '320px'],
            content: ['smg/update?stationId=' + data['stationid'], 'no']
        })
    }

    /**
     * 用于删除后台站点
     * @param data
     */
    function delstation(data) {
        var index = top.layer.confirm('数据删除后无法恢复，确认删除？', {icon: 3, title: '警告'}, function (index) {
            $.ajax({
                url: "/smg/del?stationId=" + data["stationid"],
                type: "get",
                timeout: 5000,
                success: function () {
                    table.reload("datatable", {})
                }
            })
            top.layer.close(index);
        });
    }

    /**
     * 监听编辑事件
     */
    table.on('edit(list)', function (obj) {
        var value = obj.value //得到修改后的值
            , data = obj.data //得到所在行所有键值
            , field = obj.field; //得到字段
        $.post("../smg/updatestationState", JSON.stringify(data), function (result) {
        });
    });
    table.on('rowDouble(list)', function (obj) {
        console.log(obj);
    });
    /**
     * 监听按钮工具栏
     */
    table.on('tool(list)', function (obj) {
        var data = obj.data //获得当前行数据
            , layEvent = obj.event; //获得 lay-event 对应的值
        if (mylayer != null) {
            top.layer.close(mylayer);
            mylayer = null;
        }
        if (layEvent === 'edit') {
            mylayer = top.layer.open({
                type: 2,
                title: "设置地图显示要素 [ " + data['stationid'] + "_" + data['alias'] + " ]",
                shadeClose: true,
                shade: false,
                maxmin: false, //开启最大化最小化按钮
                area: ['800px', 'auto'],
                //  area:'auto',
                content: 'smg/edit?stationId=' + data['stationid'],
                success: function (layero, index) {
                    top.layer.iframeAuto(index);
                }
            });
        } else if (layEvent == 'cmd') {
            mylayer = top.layer.open({
                type: 2,
                title: "管理[" + data['stationid'] + "]",
                shadeClose: true,
                shade: false,
                maxmin: false, //开启最大化最小化按钮
                area: ['680px', '480px'],
                content: 'smg/cmd?stationId=' + data['stationid']
            });
        } else if (layEvent == 'update') {
            if ($.cookie('update')) {
                openupdate(layer, data);
            } else {
                top.layer.prompt({title: '请输入升级密码', formType: 1}, function (pass, index) {
                    top.layer.close(index);
                    if ('ldvis123' == pass) {
                        $.cookie('update', 1, {
                            path: '/',           //cookie的作用域
                            //expires : expiresDate
                        });
                        openupdate(layer, data);
                    } else {
                        layer.msg("密码错误");
                    }
                });
            }
        } else if (layEvent == 'del') {
            if ($.cookie('del')) {
                delstation(data);
            } else {
                top.layer.prompt({title: '请输入删除密码', formType: 1}, function (pass, index) {
                    top.layer.close(index);
                    if ('ldvis123' == pass) {
                        $.cookie('del', 1, {
                            path: '/',           //cookie的作用域
                            //expires : expiresDate
                        });
                        delstation(data);
                    } else {
                        layer.msg("密码错误");
                    }
                });
            }
        }
    });
});



