package com.ldchina.datacenter.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)// 启用 Spring Security 全局方法
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    private UserDetailsServiceImp userDetailsService;
//
//    @Autowired
//    private MysuccessHandle mysuccessHandle;

    // 该方法定义认证用户信息获取的来源、密码校验的规则
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//        //auth.authenticationProvider(myauthenticationProvider)  自定义密码校验的规则
//
//        //如果需要改变认证的用户信息来源，我们可以实现UserDetailsService
//  //      auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
//
//        //inMemoryAuthentication 从内存中获取
//        //	auth.inMemoryAuthentication().withUser("chengli").password("123456").roles("USER");
//
//        //jdbcAuthentication从数据库中获取，但是默认是以security提供的表结构
//        //usersByUsernameQuery 指定查询用户SQL
//        //authoritiesByUsernameQuery 指定查询权限SQL
//        //auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(query).authoritiesByUsernameQuery(query);
//
//        //注入userDetailsService，需要实现userDetailsService接口
//        //auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
//    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin()
                .loginPage("/login") //定义登录页面 或 转到登录页的url
                //.loginProcessingUrl("/user/login")//自定义 表单提交登录的url  默认 /login
                //.usernameParameter("userName")//自定义登录参数名 默认为 username
                //.passwordParameter("passWord")//自定义登录参数名 默认为 password
                .defaultSuccessUrl("/") //默认登录成功 url
                 //.successForwardUrl("/") //登录成功的重定向 只能设置控制层url 不能设置为页面   如果同时设置了 defaultSuccessUrl successForwardUrl 后面的会覆盖前面的设置
                // .successHandler(mysuccessHandle) //登录成功的回调，可以自定义处理  如果同时设置了其他成功处理  最后的总会覆盖前面的设置
                .failureUrl("/login?failed=true")  //登录失败系统转向的url ，默认是this.loginPage + "?error"。这里有个坑，这里默认是没有权限的！ 我们必须给它额外配置一个适当的登录权限，或者设置不需要保护。否则是跳转不过去的，会跳转到登录页面
                //.failureForwardUrl("/error1") //登录失败的重定向  failureUrl failureForwardUrl  同时设置时 后面的设置会覆盖前面的设置。
                // .failureHandler(myfailureHandler) //登录失败的回调 如果同时设置了其他的失败处理 最后设置的会覆盖前面的
                //defaultSuccessUrl successForwardUrl successHandler 基本只使用其中一个就可以了
                //failureUrl  failureForwardUrl  failureHandler
                .and()
                .authorizeRequests() //定义哪些url 是否需要保护
                .antMatchers("/login","/static/**","/api/*").permitAll()  //ant 匹配路径参数格式// 路径 /login.htm  可以被任何人访问到

                .anyRequest() //其他任何请求
                .authenticated() //需要在登录验证后才可以访问
                .and()
                .headers().frameOptions().disable() //项目中用到iframe嵌入网页时 ，需要关闭
                .and()
                .csrf().disable();//关闭csrf 防护


//        http.logout().logoutUrl("/mylogout") //指定登出的url
//                .logoutSuccessUrl("/home")//登出转到的页面
//                .permitAll();

//        http.exceptionHandling().accessDeniedHandler((HttpServletRequest httpServletRequest, HttpServletResponse response, AccessDeniedException e)->{ //权限不足处理
//
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setCharacterEncoding("UTF-8");
//            PrintWriter out = response.getWriter();
//            out.write("{\"status\":\"error\",\"msg\":\"权限不足，请联系管理员!\"}");
//            out.flush();
//            out.close();
//
//        });// 权限不足的回调
           //     .authenticationEntryPoint(myAuthenticationEntryPoint);// 未登录或者登陆过期、未授权 的拦截回调



        //设置同一账号仅一次登录 后面别处登录会剔除前面的登录。
        //需要重写 实现了UserDetails的User的三个方法
//        @Override
//        public String toString() {
//            return this.userName;
//        }
//
//        @Override
//        public int hashCode() {
//            return this.userName.hashCode();
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            return this.toString().equals(obj.toString());
//        }
//        http.sessionManagement()//Session管理器
//                .maximumSessions(1)
//                .expiredUrl("/expiredPage");//被踢出时转到得页面  默认没有权限 所以需要配置不需要认证 不然访问不到






        //高级配置方式
//        .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
//             @Override
//             public <O extends FilterSecurityInterceptor> O postProcess(O o) {
//                        o.setSecurityMetadataSource(myFilterInvocationSecurityMetadataSource);/根据一个url请求，获得访问它所需要的roles权限
//                        o.setAccessDecisionManager(myAccessDecisionManager);//接收一个用户的信息和访问一个url所需要的权限，判断该用户是否可以访问
//                        return o;
//             }

    }


//    //在这里配置哪些url 不需要认证
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/error.html"); //参数为 ant 路径格式， 直接匹配url
////        mvcMatchers 同上，但是用于匹配 @RequestMapping 的value
////        regexMatchers 同上，但是为正则表达式
//
//    }
//
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }


}