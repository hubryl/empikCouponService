# Getting Started

## Database
H2 DB : http://localhost:8080/h2-console/login.jsp

## Postman Configuration
appUrl : http://localhost:8080
apiAddress : /api/v1/coupon

## IP geolocation

https://ipinfo.io/dashboard/lite

## Swagger
http://localhost:8080/swagger-ui/index.html#/

## Building/Starting project Locally
- mvn clean package - create jar file
- docker build -t empikcouponservice:latest . - create docker file
- docker run -rm -p 8080:8080 empikcouponservice - run docker image(-rm to remove container after it stops) 

## Additinal information
### Properties
- _ipinfo.lookupip.disabled_ - for testing purposes on local env country lookup can be disabled
- _ipinfo.lookupip.token_ - token for application, should not be stored in properties file but since i do not have access to store it in safe place i decided to put it here(test purposes nor prod, on prod - not allowed!)

### Logging
- I am logging user id and ip address, in normal situation, when i have user object, i would rather log dbId instead of user information due to data safety

### API Response
- Assumption for implementation is usage by FE app. Because of that i have decided not to include detailed information regarding why given coupon was rejected. With real-life implementation i would reach to PO to check if this information are needed and to get a little bit more info regarding its usage and business value behind it. Implementation for microservices could be different as well, because, for example, other service could automatically generate new coupon when old one us fully used etc. For that reason i have separated each business case in com.empik.recruitment.couponservice.service.impl.CouponServiceImpl.canUseCoupon to be ready for change(of course CouponUseageEnum needs to be updated as well). Yes, I know YAGNI ;)
- Response priority was decided by me, but in real life would be agreed with customer, for example when we have coupon with one usage and user wants to use it for second time - system shows "Invalid Coupon" as max usages already reached.

### Scalability
- code should be scalable, additionally i have added small config for docker and kubernates to get "full" scalability

### TODO
- Performance tests (JMeter, k6) to be added, based on company policy/requirements. Needed for long term development to monitor endpoints performance.
- Move tokens to more secure place (GIT is not a place for that)
- Create documentation (if needed)
- Write POSTMAN tests (if needed) - i have supported postman config to make it easier(I am also using insomnia, but i feel like Postman is more commonly used)
- BeanUtils.copyProperties - is used in this situation to be easier, for bigger items normal mapping would be advised.
