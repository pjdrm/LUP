/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package l2f.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Contains utilities related to manipulating classes.
 *
 * From ProgramD AIML interpreter
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class ClassUtils
{
    /**
     * Returns the class which is a subclass of <code>T</code>,
     * instantiated using a constructor that takes the arguments given.
     * @param classname the classname to instantiate
     * @param <T> the class of which the instantiated class must be a subclass
     * @param baseType the base class type
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSubclassInstance(Class<T> baseType, String classname, String description, Object ... constructorArgs)
    {
        // Get the subclass.
        Class<? extends T> subclass = null;
        try
        {
            subclass = (Class<? extends T>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
        	System.err.println("ClassUtils: Specified " + description + " (\"" + classname + "\") could not be found. Exception: "+e);
        	System.exit(-1);
        }
        catch (ClassCastException e)
        {
            System.err.println("\"" + classname
                    + "\" is not a " + description + "subclass. Exception: " +  e);
            System.exit(-1);
        }
        
        return getNewInstance(subclass, description, constructorArgs);
    }
    
    /**
     * Returns an instance of the given class,
     * instantiated using a constructor that takes the arguments given.
     * @param <T> the type of the class
     * @param theClass the class to instantiate
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNewInstance(Class<T> theClass, String description, Object ... constructorArgs)
    {
        // Get the types of the arguments.
        int argCount = constructorArgs.length;
        ArrayList<Class<T>> argumentTypes = new ArrayList<Class<T>>(argCount);
        for (int index = 0; index < argCount; index++)
        {
            try
            {
                argumentTypes.add((Class<T>) constructorArgs[index].getClass());
            }
            catch (ClassCastException e)
            {
            	System.err.println("ClassUtils: Invalid arguments provided for constructor to create new " + description +". Exception: "+ e);
            	System.exit(-1); return null;
            }
        }

        // Get the constructor that takes the given argument types.
        Constructor<T> constructor = null;
        try
        {
            constructor = theClass.getConstructor(argumentTypes.toArray(new Class[]{}));
        }
        catch (NoSuchMethodException e)
        {
        	System.err.println("ClassUtils: Developer specified an invalid constructor for " + description +". Exception: "+ e);
        	System.exit(-1); return null;
        }
        catch (SecurityException e)
        {
        	System.err.println("ClassUtils: Permission denied to create new " + description + " with specified constructor. Exception: "+ e);
            	System.exit(-1); return null;
        }

        // Get a new instance of the class.
        try
        {
            return constructor.newInstance(constructorArgs);
        }
        catch (IllegalAccessException e)
        {
        	System.err.println("ClassUtils: Underlying constructor for " + description + " is inaccessible. Exception: "+ e);
            	System.exit(-1); return null;
        }
        catch (InstantiationException e)
        {
        	System.err.println("ClassUtils: Could not instantiate " + description + ". Exception: "+ e);
            	System.exit(-1); return null;
        }
        catch (IllegalArgumentException e)
        {
        	System.err.println("ClassUtils: Illegal argument exception when creating " + description +". Exception: "+ e);
        	System.exit(-1); return null;
        }
        catch (InvocationTargetException e)
        {
        	System.err.println("ClassUtils: Constructor threw an exception when getting a " + description + " instance from it. Exception "+ e.getTargetException());
            	System.exit(-1); return null;
        }
    }
}
