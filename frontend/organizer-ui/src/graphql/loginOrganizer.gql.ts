import { gql } from "urql";

export const LOGIN_ORGANIZER = gql`
  mutation LoginOrganizer($input: OrganizerLoginInput!) {
    loginOrganizer(input: $input) {
      accessToken
      refreshToken
      expiresIn
      tenantId
      email
      role
    }
  }
`;
