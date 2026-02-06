register: (name, email, password, confirmPassword) =>
  api.post('/auth/register', {
    name,
    email,
    password,
    confirmPassword
  })
