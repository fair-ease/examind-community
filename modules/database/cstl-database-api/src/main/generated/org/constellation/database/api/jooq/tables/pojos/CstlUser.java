/*
 *     Examind Community - An open source and standard compliant SDI
 *     https://community.examind.com/
 * 
 *  Copyright 2022 Geomatys.
 * 
 *  Licensed under the Apache License, Version 2.0 (    the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package org.constellation.database.api.jooq.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * Generated DAO object for table admin.cstl_user
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CstlUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String  login;
    private String  password;
    private String  firstname;
    private String  lastname;
    private String  email;
    private Boolean active;
    private String  avatar;
    private String  zip;
    private String  city;
    private String  country;
    private String  phone;
    private String  forgotPasswordUuid;
    private String  address;
    private String  additionalAddress;
    private String  civility;
    private String  title;
    private String  locale;

    public CstlUser() {}

    public CstlUser(CstlUser value) {
        this.id = value.id;
        this.login = value.login;
        this.password = value.password;
        this.firstname = value.firstname;
        this.lastname = value.lastname;
        this.email = value.email;
        this.active = value.active;
        this.avatar = value.avatar;
        this.zip = value.zip;
        this.city = value.city;
        this.country = value.country;
        this.phone = value.phone;
        this.forgotPasswordUuid = value.forgotPasswordUuid;
        this.address = value.address;
        this.additionalAddress = value.additionalAddress;
        this.civility = value.civility;
        this.title = value.title;
        this.locale = value.locale;
    }

    public CstlUser(
        Integer id,
        String  login,
        String  password,
        String  firstname,
        String  lastname,
        String  email,
        Boolean active,
        String  avatar,
        String  zip,
        String  city,
        String  country,
        String  phone,
        String  forgotPasswordUuid,
        String  address,
        String  additionalAddress,
        String  civility,
        String  title,
        String  locale
    ) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.active = active;
        this.avatar = avatar;
        this.zip = zip;
        this.city = city;
        this.country = country;
        this.phone = phone;
        this.forgotPasswordUuid = forgotPasswordUuid;
        this.address = address;
        this.additionalAddress = additionalAddress;
        this.civility = civility;
        this.title = title;
        this.locale = locale;
    }

    /**
     * Getter for <code>admin.cstl_user.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>admin.cstl_user.id</code>.
     */
    public CstlUser setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.login</code>.
     */
    @NotNull
    @Size(max = 255)
    public String getLogin() {
        return this.login;
    }

    /**
     * Setter for <code>admin.cstl_user.login</code>.
     */
    public CstlUser setLogin(String login) {
        this.login = login;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.password</code>.
     */
    @NotNull
    @Size(max = 32)
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter for <code>admin.cstl_user.password</code>.
     */
    public CstlUser setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.firstname</code>.
     */
    @NotNull
    @Size(max = 255)
    public String getFirstname() {
        return this.firstname;
    }

    /**
     * Setter for <code>admin.cstl_user.firstname</code>.
     */
    public CstlUser setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.lastname</code>.
     */
    @NotNull
    @Size(max = 255)
    public String getLastname() {
        return this.lastname;
    }

    /**
     * Setter for <code>admin.cstl_user.lastname</code>.
     */
    public CstlUser setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.email</code>.
     */
    @NotNull
    @Size(max = 255)
    public String getEmail() {
        return this.email;
    }

    /**
     * Setter for <code>admin.cstl_user.email</code>.
     */
    public CstlUser setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.active</code>.
     */
    @NotNull
    public Boolean getActive() {
        return this.active;
    }

    /**
     * Setter for <code>admin.cstl_user.active</code>.
     */
    public CstlUser setActive(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.avatar</code>.
     */
    @Size(max = 255)
    public String getAvatar() {
        return this.avatar;
    }

    /**
     * Setter for <code>admin.cstl_user.avatar</code>.
     */
    public CstlUser setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.zip</code>.
     */
    @Size(max = 64)
    public String getZip() {
        return this.zip;
    }

    /**
     * Setter for <code>admin.cstl_user.zip</code>.
     */
    public CstlUser setZip(String zip) {
        this.zip = zip;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.city</code>.
     */
    @Size(max = 255)
    public String getCity() {
        return this.city;
    }

    /**
     * Setter for <code>admin.cstl_user.city</code>.
     */
    public CstlUser setCity(String city) {
        this.city = city;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.country</code>.
     */
    @Size(max = 255)
    public String getCountry() {
        return this.country;
    }

    /**
     * Setter for <code>admin.cstl_user.country</code>.
     */
    public CstlUser setCountry(String country) {
        this.country = country;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.phone</code>.
     */
    @Size(max = 64)
    public String getPhone() {
        return this.phone;
    }

    /**
     * Setter for <code>admin.cstl_user.phone</code>.
     */
    public CstlUser setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.forgot_password_uuid</code>.
     */
    @Size(max = 64)
    public String getForgotPasswordUuid() {
        return this.forgotPasswordUuid;
    }

    /**
     * Setter for <code>admin.cstl_user.forgot_password_uuid</code>.
     */
    public CstlUser setForgotPasswordUuid(String forgotPasswordUuid) {
        this.forgotPasswordUuid = forgotPasswordUuid;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.address</code>.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Setter for <code>admin.cstl_user.address</code>.
     */
    public CstlUser setAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.additional_address</code>.
     */
    public String getAdditionalAddress() {
        return this.additionalAddress;
    }

    /**
     * Setter for <code>admin.cstl_user.additional_address</code>.
     */
    public CstlUser setAdditionalAddress(String additionalAddress) {
        this.additionalAddress = additionalAddress;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.civility</code>.
     */
    @Size(max = 64)
    public String getCivility() {
        return this.civility;
    }

    /**
     * Setter for <code>admin.cstl_user.civility</code>.
     */
    public CstlUser setCivility(String civility) {
        this.civility = civility;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.title</code>.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for <code>admin.cstl_user.title</code>.
     */
    public CstlUser setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Getter for <code>admin.cstl_user.locale</code>.
     */
    @NotNull
    public String getLocale() {
        return this.locale;
    }

    /**
     * Setter for <code>admin.cstl_user.locale</code>.
     */
    public CstlUser setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CstlUser (");

        sb.append(id);
        sb.append(", ").append(login);
        sb.append(", ").append(password);
        sb.append(", ").append(firstname);
        sb.append(", ").append(lastname);
        sb.append(", ").append(email);
        sb.append(", ").append(active);
        sb.append(", ").append(avatar);
        sb.append(", ").append(zip);
        sb.append(", ").append(city);
        sb.append(", ").append(country);
        sb.append(", ").append(phone);
        sb.append(", ").append(forgotPasswordUuid);
        sb.append(", ").append(address);
        sb.append(", ").append(additionalAddress);
        sb.append(", ").append(civility);
        sb.append(", ").append(title);
        sb.append(", ").append(locale);

        sb.append(")");
        return sb.toString();
    }
}
