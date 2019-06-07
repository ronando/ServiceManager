所有App模块涉及到互相调用的需在lib.in.service模块对应包名下定义Service接口，然后在具体模块中写实现类，具体可以参考login包下的UserInfoService

使用举例： 比如login模块提供当前用户id供diary模块使用
* 在lib.in.service下的login包中定义UserInfoService，继承IService接口
* 在app.login模块实现UserInfoServiceImpl类，实现UserInfoService接口
* 为实现类添加注解@ServiceImpl （如果某个服务需要以单例方式提供，声明singleton=true即可，注意：大部分情况不需要单例）
* 在调用模块通过ServiceManager.findService(UserInfoService.class).getCurrentUserId()，进行服务调用。
  
  
**注：ServiceManager.findService()永远不会返回null，所以不需要判空，但是对方法的返回值要有判空意识。**  
**注：lib.in.service模块只允许定义接口和相关结构类型，不接受接口无关的类。**