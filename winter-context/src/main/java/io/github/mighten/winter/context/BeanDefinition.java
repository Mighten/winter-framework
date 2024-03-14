package io.github.mighten.winter.context;

import io.github.mighten.winter.exception.BeanCreationException;
import lombok.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * describe a bean in Winter IoC Container
 */
@Data
public class BeanDefinition implements Comparable<BeanDefinition> {

    /**
     * bean instance to be described
     */
    private Object instance = null;

    /**
     * Bean name
     */
    private final String name;

    /**
     * if labeled with Annotation @Primary
     */
    private final boolean primary;

    /**
     * the `order` value in `@Order(value = ${order})`
     */
    private final int order;

    /**
     * bean class
     */
    private final Class<?> beanClass;

    /**
     * Class Constructor of the bean
     */
    private final Constructor<?> constructor;

    /**
     * init method
     */
    private String initMethodName;
    private Method initMethod;

    /**
     * destroy method
     */
    private String destroyMethodName;
    private Method destroyMethod;

    /**
     * Factory method
     */
    private final String factoryName;
    private final Method factoryMethod;


    /**
     *  get non-null bean instance
     * @return the non-null bean instance
     * @throws BeanCreationException if bean instance is not instantiated
     */
    public Object getRequiredInstance() throws BeanCreationException {
        if (this.instance == null)
            throw new BeanCreationException(
                    String.format("Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                            this.getName(),
                            this.getBeanClass().getName()));
        return this.instance;
    }


    /**
     * Set the a non-null instance for the bean
     * @param instance the instantiated bean
     */
    public void setInstance(Object instance) {
        Objects.requireNonNull(instance, "Bean instance is null.");

        if (!this.beanClass.isAssignableFrom(instance.getClass()))
            throw new BeanCreationException(
                    String.format("Instance '%s' of Bean '%s' is not the expected type: %s",
                            instance,
                            instance.getClass().getName(),
                            this.beanClass.getName()));
        this.instance = instance;
    }

    /**
     * check if the current bean annotated with @Primary
     * @return true if this bean is annotated with @Primary
     */
    public boolean isPrimary() {
        return this.primary;
    }


    /**
     * get factory method metadata
     * @return factory method creation details for the bean
     */
    String getFactoryMethodCreationDetail() {

        if (this.getFactoryMethod() == null)
            return "";

        String[] factoryMethodNames = Arrays.stream(this.factoryMethod.getParameterTypes())
                .map(Class::getSimpleName)
                .toArray(String[]::new);

        String params = String.join(", ", factoryMethodNames);
        return this.getFactoryMethod().getDeclaringClass().getSimpleName()
                + "." + this.getFactoryMethod().getName()
                + "(" + params + ")";
    }


    /**
     * initialize bean without factory method
     * @param name bean name
     * @param beanClass bean class
     * @param constructor ctor
     * @param order the priority of bean in Winter IoC Container
     * @param primary if the bean annotated with @Primary
     * @param initMethodName initialization method name
     * @param destroyMethodName destroy method name
     * @param initMethod initialization method
     * @param destroyMethod destroy method
     */
    public BeanDefinition(String name, Class<?> beanClass,
                          Constructor<?> constructor,
                          int order,
                          boolean primary,
                          String initMethodName,
                          String destroyMethodName,
                          Method initMethod,
                          Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = constructor;
        this.factoryName = null;
        this.factoryMethod = null;
        this.order = order;
        this.primary = primary;
        constructor.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }


    /**
     * initialize the bean with factory method
     * @param name bean name
     * @param beanClass bean class
     * @param order the priority of bean in Winter IoC Container
     * @param primary if the bean annotated with @Primary
     * @param initMethodName initialization method name
     * @param destroyMethodName destroy method name
     * @param initMethod initialization method
     * @param destroyMethod destroy method
     * @param factoryName factory name
     * @param factoryMethod factory method
     */
    public BeanDefinition(String name,
                          Class<?> beanClass,
                          String factoryName, Method factoryMethod,
                          int order,
                          boolean primary,
                          String initMethodName,
                          String destroyMethodName,
                          Method initMethod,
                          Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = null;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = order;
        this.primary = primary;
        factoryMethod.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }


    /**
     * initialize init and destroy methods
     * @param initMethodName name for init method
     * @param destroyMethodName name for destroy method
     * @param initMethod init method
     * @param destroyMethod destroy method
     */
    private void setInitAndDestroyMethod(String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
        if (initMethod != null)
            initMethod.setAccessible(true);
        if (destroyMethod != null)
            destroyMethod.setAccessible(true);
    }


    /**
     * Compares this object with the specified object for order.
     *     sort by `order` in ascending order,
     *      if two `order`-s are equal, then sort `name` lexicographically
     *
     * @param beanDefinition the bean to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     */
    @Override
    public int compareTo(BeanDefinition beanDefinition) {
        int orderComparison = Integer.compare(this.order, beanDefinition.getOrder());
        if (orderComparison != 0)
            return orderComparison;
        return  this.name.compareTo(beanDefinition.getName() );
    }
}
