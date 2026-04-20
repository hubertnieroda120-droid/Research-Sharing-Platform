const base = {
    get() {
        return {
            url : "http://localhost:8080/springbootl33wm/",
            name: "springbootl33wm",
            // 退出到首页链接
            indexUrl: ''
        };
    },
    getProjectName(){
        return {
            projectName: "科研数据共享平台"
        } 
    }
}
export default base
