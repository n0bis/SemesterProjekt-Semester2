import jwt_decode from "jwt-decode";

export const hasRole = role => {
  const token = localStorage.getItem("authorization");
  if (token == null) {
    return false;
  }
  const userData = jwt_decode(token);
  console.log(userData);
  if (userData.authorities == `ROLE_${role}`) {
    return true;
  }
  return false;
};

export const hasAnyRole = roles => {
  const token = localStorage.getItem("authorization");
  if (token == null) {
    return false;
  }
  const userData = jwt_decode(token);
  if (roles.some(role => userData.authorities.indexOf(`ROLE_${role}`) >= 0)) {
  return true;
  }
  return false;
}

export default hasAnyRole;