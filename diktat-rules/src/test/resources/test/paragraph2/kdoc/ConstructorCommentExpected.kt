package test.paragraph2.kdoc

/**
 * kdoc
 * class
 * comment
 * @property name
 */
class A constructor(
    val name: String
) {}

/**
 * kdoc
 * class
 * comment
 * @property name single-line comment
 */
class A constructor(
    val name: String
) {}

/**
 * kdoc
 * class
 * comment
 * @property name
 * block
 * comment
 */
class A constructor(
    val name: String
) {}

/**
 * kdoc
 * class
 * comment
 * @property name
 * kdoc property
 * comment
 */
class A constructor(
    val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /**
     * @property name property
     * comment
     */
    val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    private val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    //single-line comment
    private val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /*
     * block
     * comment
     */
    private val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /**
     * kdoc property
     * comment
     */
    private val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /**
     * @property name property
     * comment
     */
    private val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    override val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    //single-line comment
    override val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /*
     * block
     * comment
     */
    override val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /**
     * kdoc property
     * comment
     */
    override val name: String
) {}

/**
 * kdoc
 * class
 * comment
 */
class A constructor(
    /**
     * @property name property
     * comment
     */
    override val name: String
) {}

/**
 * kdoc
 * class
 * comment
 * @property openName single-line comment
 * @property openLastName
 * block
 * comment
 * @property openBirthDate
 * kdoc property
 * comment
 */
open class B constructor(
    open val openName: String,
    open val openLastName: String,
    open val openBirthDate: String,
    /**
     * @property openAddr property
     * comment
     */
    open val openAddr: String
) {}

/**
 * kdoc
 * class
 * comment
 * @property protectedName single-line comment
 * @property internalName single-line comment
 * @property name single-line comment
 * @property protectedLastName
 * block
 * comment
 * @property internalLastName
 * block
 * comment
 * @property lastName
 * block
 * comment
 * @property protectedBirthDate
 * kdoc property
 * comment
 * @property internalBirthDate
 * kdoc property
 * comment
 * @property birthDate
 * kdoc property
 * comment
 */
class A constructor(
    //single-line comment
    private val privateName: String,
    protected val protectedName: String,
    internal val internalName: String,
    //single-line comment
    override val openName: String,
    val name: String,
    /*
     * block
     * comment
     */
    private val privateLastName: String,
    protected val protectedLastName: String,
    internal val internalLastName: String,
    /*
     * block
     * comment
     */
    override val openLastName: String,
    val lastName: String,
    /**
     * kdoc property
     * comment
     */
    private val privateBirthDate: String,
    protected val protectedBirthDate: String,
    internal val internalBirthDate: String,
    /**
     * kdoc property
     * comment
     */
    override val openBirthDate: String,
    val birthDate: String,
    /**
     * @property privateAddr property
     * comment
     */
    private val privateAddr: String,
    /**
     * @property protectedAddr property
     * comment
     */
    protected val protectedAddr: String,
    /**
     * @property internalAddr property
     * comment
     */
    internal val internalAddr: String,
    /**
     * @property openAddr property
     * comment
     */
    override val openAddr: String,
    /**
     * @property addr property
     * comment
     */
    val addr: String,
) : B() {}
