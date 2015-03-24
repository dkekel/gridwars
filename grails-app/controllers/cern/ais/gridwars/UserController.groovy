package cern.ais.gridwars

import org.apache.commons.codec.digest.DigestUtils

class UserController
{

  def register = {
    // new user posts his registration details
    if (request.method == 'POST')
    {
      // create domain object and assign parameters using data binding
      def u = new User(params)
      u.salt = Math.round(Math.random() * 10000).toString()
      u.hashedPassword = DigestUtils.sha256Hex(u.salt + params.password)

      if (!u.save(validate: true, flush: true))
      {
        // validation failed, render registration page again
        return [user: u]
      }
      else
      {
        // validate/save ok, store user in session, redirect to homepage
        session.user = u
        redirect(controller: 'main')
      }
    }
    else if (session.user)
    {
      // don't allow registration while user is logged in
      redirect(controller: 'main')
    }
  }

  def login = {
    if (request.method == 'POST')
    {
      def user = User.findByUsername(params.username)
      if (user && user.hashedPassword.equals(DigestUtils.sha256Hex(user.salt + params.password)))
      {
        // username and password match -> log in
        session.user = user
        redirect(controller: 'main')
      }
      else
      {
        // nope
        redirect(controller: 'main')
      }
    }
    else if (session.user)
    {
      // don't allow login while user is logged in
      redirect(controller: 'main')
    }
  }

  def logout = {
    session.invalidate()
    redirect(controller: 'main')
  }
}
