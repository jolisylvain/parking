package org.friends.app.view.route;

import java.security.AccessControlException;

import org.friends.app.Configuration;
import org.friends.app.model.User;
import org.friends.app.service.impl.UserServiceBean;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;
import spark.utils.StringUtils;

public abstract class AuthenticatedRoute implements TemplateViewRoute {

	UserServiceBean userService = new UserServiceBean();
	
	@Override
	public ModelAndView handle(Request request, Response response) throws Exception {
		checkAuthenticated(request, response);
		return doHandle(request, response);
	}

	protected abstract ModelAndView doHandle(Request request, Response response);

	private void checkAuthenticated(Request request, Response response) {
		User user = getUser(request);
		
		// 1 : try to find user in session
		if (user != null)
			return;
		
		// 2. Try to find user using cookie
		boolean userFound = false;
		String cookie = request.cookie(Configuration.COOKIE);
		if (!StringUtils.isEmpty(cookie)) {
			user = userService.findUserByCookie(cookie);
			if (user != null) {
				request.session().attribute("user", user);
				userFound = true;
			} else {
				// Clean cookie if no user
				response.removeCookie(Configuration.COOKIE);
			}
		}

		// Stop route
		if (!userFound) {
			throw new AccessControlException("User not authenticated");
		}
	}

	protected User getUser(Request request) {
		return request.session().attribute("user");
	}

}
