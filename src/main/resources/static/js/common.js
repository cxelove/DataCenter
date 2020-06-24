var NoVal = -999;
/**
 * 对Date的扩展，将 Date 转化为指定格式的String
 * 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
 * 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
 * 例子：
 * (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
 * (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
 * @param fmt
 * @returns {*}
 * @constructor
 */
Date.prototype.Format = function(fmt)
{ //author: meizz
    var o = {
        "M+" : this.getMonth()+1,                 //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                   //小时
        "m+" : this.getMinutes(),                 //分
        "s+" : this.getSeconds(),                 //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()             //毫秒
    };
    if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
}


function getRootPath_web() {
    var arr = window.document.referrer.split("/");
    var last = arr[arr.length-1];
    var ur= window.document.referrer.substr(0,window.document.referrer.indexOf(last));
    return ur.substr(ur.indexOf(window.document.location.host));
    // //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
    // var curWwwPath = window.document.location.href;
    // //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
    // var pathName = window.document.location.pathname;
    // var pos = curWwwPath.indexOf(pathName);
    // //获取主机地址，如： http://localhost:8083
    // var localhostPaht = curWwwPath.substring(0, pos);
    // //获取带"/"的项目名，如：/uimcardprj
    // var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    // return (localhostPaht + projectName);
}
