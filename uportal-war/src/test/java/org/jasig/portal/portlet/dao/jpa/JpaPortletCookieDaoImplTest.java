/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link JpaPortletCookieDaoImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@SuppressWarnings("deprecation")
@Ignore
public class JpaPortletCookieDaoImplTest extends BaseJpaDaoTest {
	
	private IPortletCookieDao portletCookieDao;
	
    public JpaPortletCookieDaoImplTest() {
        this.setDependencyCheck(false);
    }
	/*
	 * (non-Javadoc)
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }

	/**
	 * @param portletCookieDao the portletCookieDao to set
	 */
	public void setPortletCookieDao(IPortletCookieDao portletCookieDao) {
		this.portletCookieDao = portletCookieDao;
	}


	/**
	 * 
	 */
	@Test
	public void testPortalCookieLifeCycle() {
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
        		IPortalCookie newCookie = portletCookieDao.createPortalCookie();
        		Assert.assertNotNull(newCookie);
        		final String cookieValue = newCookie.getValue();
        		Assert.assertEquals(0, newCookie.getPortletCookies().size());
        		Assert.assertEquals(40, newCookie.getValue().length());
        		Assert.assertNotNull(newCookie.getCreated());
        		Assert.assertNotNull(newCookie.getExpires());
        		
//        		this.checkPoint();
        		
        		IPortalCookie retrieved = portletCookieDao.getPortalCookie(cookieValue);
        		Assert.assertNotNull(retrieved);
        		Assert.assertEquals(newCookie, retrieved);
        		
        		Date newExpiration = DateUtils.addHours(retrieved.getExpires(), 2);
        		
        		portletCookieDao.updatePortalCookieExpiration(retrieved, newExpiration);
        		
//        		this.checkPoint();
        		
        		IPortalCookie retrieved2 = portletCookieDao.getPortalCookie(cookieValue);
        		Assert.assertEquals(newExpiration, retrieved2.getExpires());
        		Assert.assertEquals(retrieved.getCreated(), retrieved2.getCreated());
        		
        		portletCookieDao.deletePortalCookie(retrieved2);
        		
//        		this.checkPoint();
        		
        		IPortalCookie gone = portletCookieDao.getPortalCookie(cookieValue);
        		Assert.assertNull(gone);
        		
        		return null;
            }
        });
	}
	
	/**
	 * 
	 */
	@Test
	public void testPortletCookieLifeCycle() {
        final String portalCookieValue = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                IPortalCookie portalCookie = portletCookieDao.createPortalCookie();
                final String portalCookieValue = portalCookie.getValue();
        		Assert.assertNotNull(portalCookie);
        		
        		return portalCookie.getValue();
            }
        });

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
        		
        		Cookie cookie = new Cookie("cookieName1", "cookieValue1");
        		portletCookieDao.storePortletCookie(portalCookie, cookie);
        		
        		return null;
            }
        });
        


        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
                
                Assert.assertEquals(1, portalCookie.getPortletCookies().size());
        		IPortletCookie portletCookie1 = new ArrayList<IPortletCookie>(portalCookie.getPortletCookies()).get(0);
        		Assert.assertEquals("cookieName1", portletCookie1.getName());
        		Assert.assertEquals("cookieValue1", portletCookie1.getValue());
        		Assert.assertFalse(portletCookie1.isSecure());
        		
        		Cookie cookie = new Cookie("cookieName1", "cookieValue1");
                cookie.setSecure(true);
        		portletCookieDao.updatePortletCookie(portalCookie, cookie);
                
                return null;
            }
        });
        

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
                
                Assert.assertEquals(1, portalCookie.getPortletCookies().size());
                IPortletCookie portletCookie1 = new ArrayList<IPortletCookie>(portalCookie.getPortletCookies()).get(0);
        		Assert.assertEquals("cookieName1", portletCookie1.getName());
        		Assert.assertEquals("cookieValue1", portletCookie1.getValue());
        		Assert.assertTrue(portletCookie1.isSecure());
        		
        		Cookie cookie2 = new Cookie("cookieName2", "cookieValue2");
        		portalCookie = portletCookieDao.storePortletCookie(portalCookie, cookie2);
                
                return null;
            }
        });
        

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
        		
        		Assert.assertEquals(2, portalCookie.getPortletCookies().size());
        		
        		
        		Cookie cookie = new Cookie("cookieName1", "cookieValue1");
                portletCookieDao.deletePortletCookie(portalCookie, cookie);
                
                return null;
            }
        });
        

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
                
        		Assert.assertEquals(1, portalCookie.getPortletCookies().size());
        		IPortletCookie portletCookie1 = new ArrayList<IPortletCookie>(portalCookie.getPortletCookies()).get(0);
        		Assert.assertEquals("cookieName2", portletCookie1.getName());
        		Assert.assertEquals("cookieValue2", portletCookie1.getValue());
        		
        		Cookie cookie2 = new Cookie("cookieName2", "cookieValue2");
                portletCookieDao.deletePortletCookie(portalCookie, cookie2);
                
                return null;
            }
        });
        

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                IPortalCookie portalCookie = portletCookieDao.getPortalCookie(portalCookieValue);
                
        		Assert.assertEquals(0, portalCookie.getPortletCookies().size());
        		
        		return null;
            }
        });
	}
}